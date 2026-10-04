package com.proteintracker.domain

import com.proteintracker.data.local.ProteinEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProteinMathTest {

    private val today = LocalDate.of(2026, 9, 30)

    private fun entry(total: Double, day: LocalDate = today) = ProteinEntry(
        id = 1L,
        name = "Test",
        amountGrams = 100.0,
        proteinPer100g = total,
        totalProtein = total,
        iconKey = "restaurant",
        loggedAt = 0L,
        dayKey = day.toString(),
    )

    @Test
    fun `totalProtein scales the per-100g value by the amount`() {
        assertEquals(46.5, ProteinMath.totalProtein(150.0, 31.0), 1e-9)
        assertEquals(0.0, ProteinMath.totalProtein(0.0, 31.0), 1e-9)
        assertEquals(31.0, ProteinMath.totalProtein(100.0, 31.0), 1e-9)
    }

    @Test
    fun `progress is clamped between zero and one`() {
        assertEquals(0.5f, ProteinMath.progress(50.0, 100.0), 1e-6f)
        assertEquals(1.0f, ProteinMath.progress(250.0, 100.0), 1e-6f)
        assertEquals(0.0f, ProteinMath.progress(0.0, 100.0), 1e-6f)
    }

    @Test
    fun `ratio is zero when the goal is not positive`() {
        assertEquals(0.0, ProteinMath.ratio(50.0, 0.0), 1e-9)
        assertEquals(0.0, ProteinMath.ratio(50.0, -10.0), 1e-9)
    }

    @Test
    fun `goal is reached on or above the goal`() {
        assertFalse(ProteinMath.isGoalReached(99.9, 100.0))
        assertTrue(ProteinMath.isGoalReached(100.0, 100.0))
        assertTrue(ProteinMath.isGoalReached(140.0, 100.0))
        assertFalse(ProteinMath.isGoalReached(500.0, 0.0))
    }

    @Test
    fun `band boundaries match the documented percentages`() {
        val date = today
        assertEquals(GoalBand.EMPTY, ProteinMath.band(0.0, 100.0, date, today))
        assertEquals(GoalBand.LOW, ProteinMath.band(39.9, 100.0, date, today))
        assertEquals(GoalBand.MEDIUM, ProteinMath.band(40.0, 100.0, date, today))
        assertEquals(GoalBand.MEDIUM, ProteinMath.band(74.9, 100.0, date, today))
        assertEquals(GoalBand.HIGH, ProteinMath.band(75.0, 100.0, date, today))
        assertEquals(GoalBand.HIGH, ProteinMath.band(99.9, 100.0, date, today))
        assertEquals(GoalBand.MET, ProteinMath.band(100.0, 100.0, date, today))
        assertEquals(GoalBand.MET, ProteinMath.band(1000.0, 100.0, date, today))
    }

    @Test
    fun `future days are never banded`() {
        val tomorrow = today.plusDays(1)
        assertEquals(GoalBand.EMPTY, ProteinMath.band(500.0, 100.0, tomorrow, today))
    }

    @Test
    fun `bands follow the goal`() {
        val date = today
        // Same total, different goals => different bands.
        assertEquals(GoalBand.HIGH, ProteinMath.band(80.0, 100.0, date, today))
        assertEquals(GoalBand.MEDIUM, ProteinMath.band(80.0, 150.0, date, today))
        assertEquals(GoalBand.LOW, ProteinMath.band(80.0, 1000.0, date, today))
        assertEquals(GoalBand.MET, ProteinMath.band(80.0, 60.0, date, today))
    }

    @Test
    fun `totalOf sums entries`() {
        assertEquals(
            61.5,
            ProteinMath.totalOf(listOf(entry(31.0), entry(30.5))),
            1e-9,
        )
        assertEquals(0.0, ProteinMath.totalOf(emptyList()), 1e-9)
    }

    @Test
    fun `formatters use a decimal comma`() {
        assertEquals("85,2", formatGrams(85.24))
        assertEquals("150", formatGrams(150.0, decimals = 0))
        assertEquals("0", formatGrams(0.0, decimals = 0))
    }

    @Test
    fun `parseDecimal accepts comma and dot`() {
        assertEquals(31.5, parseDecimal("31,5")!!, 1e-9)
        assertEquals(31.5, parseDecimal(" 31.5 ")!!, 1e-9)
        assertEquals(null, parseDecimal("abc"))
        assertEquals(null, parseDecimal(""))
    }
}
