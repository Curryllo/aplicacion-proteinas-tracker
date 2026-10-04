package com.proteintracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proteintracker.ProteinTrackerApp
import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.data.repository.ProteinStore
import com.proteintracker.data.repository.SettingsStore
import com.proteintracker.domain.ProteinMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ProteinViewModel(
    private val proteinStore: ProteinStore,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val today = MutableStateFlow(LocalDate.now())
    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val selectedDay = MutableStateFlow<LocalDate?>(null)
    private val editingEntry = MutableStateFlow<ProteinEntry?>(null)
    private val celebrate = MutableStateFlow(false)

    private val goalGrams: StateFlow<Double> = settingsStore.goalGrams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DEFAULT_GOAL)

    private val todayEntries: StateFlow<List<ProteinEntry>> = today
        .flatMapLatest(proteinStore::observeDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val monthTotals: StateFlow<Map<LocalDate, Double>> = visibleMonth
        .flatMapLatest { month ->
            proteinStore.observeTotalsIn(month.atDay(1), month.atEndOfMonth())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    private val selectedDayEntries: StateFlow<List<ProteinEntry>> = selectedDay
        .flatMapLatest { day -> day?.let { proteinStore.observeDay(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private data class Selection(
        val today: LocalDate,
        val goal: Double,
        val month: YearMonth,
        val selected: LocalDate?,
    )

    private data class Data(
        val todayEntries: List<ProteinEntry>,
        val monthTotals: Map<LocalDate, Double>,
        val selectedEntries: List<ProteinEntry>,
        val editing: ProteinEntry?,
        val celebrate: Boolean,
    )

    private val context = combine(today, goalGrams, visibleMonth, selectedDay, ::Selection)

    private val data = combine(
        todayEntries,
        monthTotals,
        selectedDayEntries,
        editingEntry,
        celebrate,
        ::Data,
    )

    val uiState: StateFlow<ProteinUiState> =
        combine(context, data) { ctx, payload ->
            ProteinUiState(
                today = ctx.today,
                visibleMonth = ctx.month,
                goalGrams = ctx.goal,
                todayEntries = payload.todayEntries,
                todayTotal = ProteinMath.totalOf(payload.todayEntries),
                monthTotals = payload.monthTotals,
                selectedDay = ctx.selected,
                selectedDayEntries = payload.selectedEntries,
                editingEntry = payload.editing,
                celebrate = payload.celebrate,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ProteinUiState.initial())

    init {
        // Fires the celebration the first time the day's total reaches the goal.
        viewModelScope.launch {
            combine(todayEntries, goalGrams) { entries, goal ->
                ProteinMath.totalOf(entries) to goal
            }.collect { (total, goal) -> maybeCelebrate(total, goal) }
        }
    }

    private suspend fun maybeCelebrate(total: Double, goal: Double) {
        val date = today.value
        if (!ProteinMath.isGoalReached(total, goal)) return
        if (settingsStore.lastCelebratedDay() == date.toString()) return
        settingsStore.setLastCelebratedDay(date.toString())
        celebrate.value = true
    }

    /** Re-reads the calendar date, e.g. after midnight or a timezone change. */
    fun refreshToday() {
        val now = LocalDate.now()
        if (now != today.value) {
            today.value = now
            visibleMonth.value = YearMonth.from(now)
        }
    }

    fun showMonth(offsetMonths: Long) {
        visibleMonth.value = visibleMonth.value.plusMonths(offsetMonths)
    }

    fun goToCurrentMonth() {
        visibleMonth.value = YearMonth.now()
    }

    fun selectDay(date: LocalDate?) {
        selectedDay.value = date
    }

    fun startEditing(entry: ProteinEntry) {
        editingEntry.value = entry
    }

    fun cancelEditing() {
        editingEntry.value = null
    }

    fun saveEntry(
        name: String,
        amountGrams: Double,
        proteinPer100g: Double,
        iconKey: String,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || amountGrams <= 0.0 || proteinPer100g < 0.0) return

        viewModelScope.launch {
            val existing = editingEntry.value
            val entry = ProteinEntry(
                id = existing?.id ?: 0L,
                name = trimmed,
                amountGrams = amountGrams,
                proteinPer100g = proteinPer100g,
                totalProtein = ProteinMath.totalProtein(amountGrams, proteinPer100g),
                iconKey = iconKey,
                loggedAt = existing?.loggedAt ?: System.currentTimeMillis(),
                dayKey = existing?.dayKey ?: today.value.toString(),
            )
            if (existing == null) proteinStore.insert(entry) else proteinStore.update(entry)
            editingEntry.value = null
        }
    }

    fun deleteEntry(entry: ProteinEntry) {
        viewModelScope.launch { proteinStore.delete(entry.id) }
    }

    /** Re-inserts a deleted entry (used by the "undo" action). */
    fun restoreEntry(entry: ProteinEntry) {
        viewModelScope.launch { proteinStore.insert(entry) }
    }

    fun setGoal(grams: Double) {
        if (grams <= 0.0) return
        viewModelScope.launch { settingsStore.setGoal(grams) }
    }

    fun dismissCelebration() {
        celebrate.value = false
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val DEFAULT_GOAL = 100.0

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as ProteinTrackerApp
                ProteinViewModel(app.container.proteinStore, app.container.settingsStore)
            }
        }
    }
}
