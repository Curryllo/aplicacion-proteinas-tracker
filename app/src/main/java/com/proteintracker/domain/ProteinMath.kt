package com.proteintracker.domain

import com.proteintracker.data.local.ProteinEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * The four heat-map bands requested for the calendar, plus the empty state.
 *
 * Bands are always relative to the *current* daily goal, so changing the goal
 * re-bands the whole history.
 */
enum class GoalBand {
    /** No entries, or a future date. */
    EMPTY,

    /** < 40 % of the goal. */
    LOW,

    /** 40 % – 74.9 % of the goal. */
    MEDIUM,

    /** 75 % – 99.9 % of the goal. */
    HIGH,

    /** >= 100 % of the goal. */
    MET,
}

object ProteinMath {

    const val BAND_HIGH_THRESHOLD = 0.75
    const val BAND_MEDIUM_THRESHOLD = 0.40

    /** Protein contributed by [amountGrams] of a food with [proteinPer100g]. */
    fun totalProtein(amountGrams: Double, proteinPer100g: Double): Double =
        amountGrams * proteinPer100g / 100.0

    /** Sum of every entry's protein. */
    fun totalOf(entries: List<ProteinEntry>): Double = entries.sumOf { it.totalProtein }

    /** Raw ratio, `0` when the goal is not a positive number. */
    fun ratio(total: Double, goal: Double): Double = if (goal > 0.0) total / goal else 0.0

    /** Ratio clamped to `0f..1f` — what the progress ring draws. */
    fun progress(total: Double, goal: Double): Float =
        ratio(total, goal).coerceIn(0.0, 1.0).toFloat()

    fun isGoalReached(total: Double, goal: Double): Boolean = goal > 0.0 && total >= goal

    fun band(total: Double, goal: Double, date: LocalDate, today: LocalDate): GoalBand {
        if (date.isAfter(today)) return GoalBand.EMPTY
        if (total <= 0.0) return GoalBand.EMPTY
        if (goal <= 0.0) return GoalBand.MET
        return when {
            total >= goal -> GoalBand.MET
            ratio(total, goal) >= BAND_HIGH_THRESHOLD -> GoalBand.HIGH
            ratio(total, goal) >= BAND_MEDIUM_THRESHOLD -> GoalBand.MEDIUM
            else -> GoalBand.LOW
        }
    }

    /**
     * Monday-first calendar grid for [month], padded out to whole weeks.
     * `null` entries are the leading/trailing blanks from the neighbouring months.
     */
    fun monthGrid(month: YearMonth): List<LocalDate?> {
        val leading = month.atDay(1).dayOfWeek.mondayFirstIndex()
        val dayCount = month.lengthOfMonth()
        val cells = ArrayList<LocalDate?>(leading + dayCount + 6)
        repeat(leading) { cells += null }
        for (day in 1..dayCount) cells += month.atDay(day)
        while (cells.size % 7 != 0) cells += null
        return cells
    }

    fun weekCount(month: YearMonth): Int = monthGrid(month).size / 7

    private fun DayOfWeek.mondayFirstIndex(): Int = (value - 1) % 7
}
