package com.proteintracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class CalendarGridTest {

    @Test
    fun `a month starting on Monday needs no leading blanks`() {
        // September 2026 starts on a Tuesday, so use a month that starts on Monday: June 2026.
        val month = YearMonth.of(2026, 6)
        assertEquals(DayOfWeek.MONDAY, month.atDay(1).dayOfWeek)

        val grid = ProteinMath.monthGrid(month)
        assertEquals(1, grid.first()!!.dayOfMonth)
        assertEquals(5, ProteinMath.weekCount(month))
    }

    @Test
    fun `a month that needs six rows gets them`() {
        // March 2026 starts on a Sunday and has 31 days => 6 weeks.
        val month = YearMonth.of(2026, 3)
        assertEquals(6, ProteinMath.weekCount(month))
        assertEquals(42, ProteinMath.monthGrid(month).size)
    }

    @Test
    fun `leading blanks put the first day in the right Monday-first column`() {
        // September 2026 starts on a Tuesday => one blank before day 1.
        val month = YearMonth.of(2026, 9)
        val grid = ProteinMath.monthGrid(month)

        assertNull(grid[0])
        assertEquals(LocalDate.of(2026, 9, 1), grid[1])
        assertEquals(DayOfWeek.TUESDAY, grid[1]!!.dayOfWeek)
    }

    @Test
    fun `a month starting on Sunday needs six leading blanks`() {
        // February 2026 starts on a Sunday.
        val month = YearMonth.of(2026, 2)
        assertEquals(DayOfWeek.SUNDAY, month.atDay(1).dayOfWeek)

        val grid = ProteinMath.monthGrid(month)
        assertEquals(6, grid.take(6).count { it == null })
        assertEquals(LocalDate.of(2026, 2, 1), grid[6])
    }

    @Test
    fun `every grid is a whole number of weeks`() {
        for (month in monthsOf2026()) {
            val grid = ProteinMath.monthGrid(month)
            assertEquals(0, grid.size % 7)
            assertTrue("grid too small for $month", grid.size >= 28)
        }
    }

    @Test
    fun `each grid contains exactly the days of its month once`() {
        for (month in monthsOf2026()) {
            val dates = ProteinMath.monthGrid(month).filterNotNull()
            assertEquals(month.lengthOfMonth(), dates.size)
            assertEquals(month.atDay(1), dates.first())
            assertEquals(month.atEndOfMonth(), dates.last())
            assertEquals(month.lengthOfMonth(), dates.distinct().size)
        }
    }

    @Test
    fun `dates are contiguous across the whole grid`() {
        for (month in monthsOf2026()) {
            val dates = ProteinMath.monthGrid(month).filterNotNull()
            dates.zipWithNext().forEach { (a, b) ->
                assertEquals(1, ChronoUnit.DAYS.between(a, b))
            }
        }
    }

    @Test
    fun `the first day lands in the column matching its weekday`() {
        // Monday-first: Monday -> column 0, Sunday -> column 6.
        for (month in monthsOf2026()) {
            val grid = ProteinMath.monthGrid(month)
            val index = grid.indexOfFirst { it != null }
            assertEquals(month.atDay(1).dayOfWeek.value - 1, index)
        }
    }

    @Test
    fun `every complete week row runs Monday to Sunday`() {
        val grid = ProteinMath.monthGrid(YearMonth.of(2026, 9))
        val completeWeeks = grid.chunked(7).filter { week -> week.none { it == null } }
        assertTrue("expected at least one complete week", completeWeeks.isNotEmpty())
        completeWeeks.forEach { week ->
            assertEquals(DayOfWeek.MONDAY, week.first()!!.dayOfWeek)
            assertEquals(DayOfWeek.SUNDAY, week.last()!!.dayOfWeek)
        }
    }

    @Test
    fun `february 2028 is a leap year and still grids evenly`() {
        val month = YearMonth.of(2028, 2)
        assertEquals(29, month.lengthOfMonth())
        val grid = ProteinMath.monthGrid(month)
        assertEquals(0, grid.size % 7)
        assertTrue(grid.contains(LocalDate.of(2028, 2, 29)))
    }

    private fun monthsOf2026(): List<YearMonth> = (1..12).map { YearMonth.of(2026, it) }
}
