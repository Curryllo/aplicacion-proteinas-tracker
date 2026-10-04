package com.proteintracker.ui

import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.data.repository.ProteinStore
import com.proteintracker.data.repository.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProteinViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private lateinit var proteinStore: FakeProteinStore
    private lateinit var settings: FakeSettingsStore

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        proteinStore = FakeProteinStore()
        settings = FakeSettingsStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adding an entry updates today's total`() = runTest(scheduler) {
        val viewModel = startedViewModel()

        viewModel.saveEntry(
            name = "  Pechuga de pollo  ",
            amountGrams = 150.0,
            proteinPer100g = 31.0,
            iconKey = "grill",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.todayEntries.size)
        assertEquals(46.5, state.todayTotal, 1e-9)
        assertEquals("Pechuga de pollo", state.todayEntries.single().name)
        assertEquals(LocalDate.now().toString(), state.todayEntries.single().dayKey)
    }

    @Test
    fun `invalid input is ignored`() = runTest(scheduler) {
        val viewModel = startedViewModel()

        viewModel.saveEntry("   ", 100.0, 20.0, "grill")
        viewModel.saveEntry("Huevo", 0.0, 20.0, "egg")
        viewModel.saveEntry("Huevo", -5.0, 20.0, "egg")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.todayEntries.isEmpty())
    }

    @Test
    fun `editing replaces the entry instead of adding one`() = runTest(scheduler) {
        val viewModel = startedViewModel()
        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()

        val original = viewModel.uiState.value.todayEntries.single()
        viewModel.startEditing(original)
        advanceUntilIdle()

        viewModel.saveEntry("Huevos", 200.0, 13.0, "egg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.todayEntries.size)
        assertEquals(26.0, state.todayTotal, 1e-9)
        assertEquals("Huevos", state.todayEntries.single().name)
        assertEquals(original.id, state.todayEntries.single().id)
        assertNull(state.editingEntry)
    }

    @Test
    fun `deleting lowers the total`() = runTest(scheduler) {
        val viewModel = startedViewModel()
        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()

        viewModel.deleteEntry(viewModel.uiState.value.todayEntries.single())
        advanceUntilIdle()

        assertEquals(0.0, viewModel.uiState.value.todayTotal, 1e-9)
    }

    @Test
    fun `month totals include the day the entry was logged`() = runTest(scheduler) {
        val viewModel = startedViewModel()
        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()

        val total = viewModel.uiState.value.monthTotals[LocalDate.now()] ?: 0.0
        assertEquals(13.0, total, 1e-9)
    }

    @Test
    fun `celebration fires once when the goal is reached`() = runTest(scheduler) {
        settings.setGoal(30.0)
        val viewModel = startedViewModel()

        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()
        assertFalse("below the goal", viewModel.uiState.value.celebrate)

        viewModel.saveEntry("Pollo", 100.0, 20.0, "grill")
        advanceUntilIdle()
        assertTrue("goal reached", viewModel.uiState.value.celebrate)
        assertEquals(LocalDate.now().toString(), settings.lastCelebratedDay())

        viewModel.dismissCelebration()
        viewModel.saveEntry("Tuna", 100.0, 25.0, "meal")
        advanceUntilIdle()
        assertFalse("must not celebrate twice on the same day", viewModel.uiState.value.celebrate)
    }

    @Test
    fun `dropping below the goal and coming back does not celebrate twice`() = runTest(scheduler) {
        settings.setGoal(30.0)
        val viewModel = startedViewModel()

        viewModel.saveEntry("Pollo", 100.0, 40.0, "grill")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.celebrate)
        viewModel.dismissCelebration()

        viewModel.deleteEntry(viewModel.uiState.value.todayEntries.single())
        advanceUntilIdle()
        viewModel.saveEntry("Pollo", 100.0, 40.0, "grill")
        advanceUntilIdle()

        assertFalse("already celebrated today", viewModel.uiState.value.celebrate)
    }

    @Test
    fun `a zero goal never celebrates`() = runTest(scheduler) {
        settings.setGoal(0.0)
        val viewModel = startedViewModel()

        viewModel.saveEntry("Pollo", 100.0, 40.0, "grill")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.celebrate)
    }

    @Test
    fun `changing the month does not change today's entries`() = runTest(scheduler) {
        val viewModel = startedViewModel()
        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()

        viewModel.showMonth(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.todayEntries.size)
        assertTrue(viewModel.uiState.value.monthTotals.isEmpty())
    }

    @Test
    fun `undo restores the deleted entry`() = runTest(scheduler) {
        val viewModel = startedViewModel()
        viewModel.saveEntry("Huevo", 100.0, 13.0, "egg")
        advanceUntilIdle()
        val removed = viewModel.uiState.value.todayEntries.single()

        viewModel.deleteEntry(removed)
        advanceUntilIdle()
        viewModel.restoreEntry(removed)
        advanceUntilIdle()

        assertEquals(13.0, viewModel.uiState.value.todayTotal, 1e-9)
    }

    private fun TestScope.startedViewModel(): ProteinViewModel {
        val viewModel = ProteinViewModel(proteinStore, settings)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        return viewModel
    }
}

private class FakeProteinStore : ProteinStore {

    private val rows = MutableStateFlow<List<ProteinEntry>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(entry: ProteinEntry): Long {
        val id = nextId++
        rows.update { it + entry.copy(id = id) }
        return id
    }

    override suspend fun update(entry: ProteinEntry) {
        rows.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun delete(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun find(id: Long): ProteinEntry? = rows.value.firstOrNull { it.id == id }

    override fun observeDay(date: LocalDate): Flow<List<ProteinEntry>> =
        rows.map { list ->
            list.filter { it.dayKey == date.toString() }.sortedByDescending { it.loggedAt }
        }

    override fun observeTotalsIn(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Double>> =
        rows.map { list ->
            list.filter { it.dayKey >= from.toString() && it.dayKey <= to.toString() }
                .groupBy { it.dayKey }
                .mapKeys { (dayKey, _) -> LocalDate.parse(dayKey) }
                .mapValues { (_, entries) -> entries.sumOf { it.totalProtein } }
        }
}

private class FakeSettingsStore : SettingsStore {

    private val goal = MutableStateFlow(100.0)
    private val celebrated = MutableStateFlow<String?>(null)

    override val goalGrams: Flow<Double> = goal

    override suspend fun setGoal(grams: Double) {
        goal.value = grams
    }

    override suspend fun lastCelebratedDay(): String? = celebrated.value

    override suspend fun setLastCelebratedDay(dayKey: String) {
        celebrated.value = dayKey
    }
}
