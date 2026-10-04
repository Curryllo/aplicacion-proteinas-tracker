package com.proteintracker.ui

import com.proteintracker.data.local.ProteinEntry
import java.time.LocalDate
import java.time.YearMonth

data class ProteinUiState(
    val today: LocalDate,
    val visibleMonth: YearMonth,
    val goalGrams: Double,
    val todayEntries: List<ProteinEntry>,
    val todayTotal: Double,
    val monthTotals: Map<LocalDate, Double>,
    val selectedDay: LocalDate?,
    val selectedDayEntries: List<ProteinEntry>,
    val editingEntry: ProteinEntry?,
    val celebrate: Boolean,
) {
    companion object {
        fun initial(today: LocalDate = LocalDate.now()): ProteinUiState = ProteinUiState(
            today = today,
            visibleMonth = YearMonth.from(today),
            goalGrams = 100.0,
            todayEntries = emptyList(),
            todayTotal = 0.0,
            monthTotals = emptyMap(),
            selectedDay = null,
            selectedDayEntries = emptyList(),
            editingEntry = null,
            celebrate = false,
        )
    }
}
