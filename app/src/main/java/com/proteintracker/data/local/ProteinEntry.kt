package com.proteintracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged food.
 *
 * [totalProtein] and [dayKey] are denormalised on purpose:
 *  - [totalProtein] lets the DAO `SUM()` protein in SQL instead of loading every row.
 *  - [dayKey] lets the calendar query a single month with an indexed range scan.
 */
@Entity(
    tableName = "protein_entries",
    indices = [Index("dayKey"), Index("loggedAt")],
)
data class ProteinEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val amountGrams: Double,
    val proteinPer100g: Double,
    val totalProtein: Double,
    val iconKey: String,
    val loggedAt: Long,
    /** Local date as `yyyy-MM-dd`. */
    val dayKey: String,
)

/** Projection for the calendar heat map: total protein per day. */
data class DayTotal(
    @ColumnInfo(name = "dayKey") val dayKey: String,
    @ColumnInfo(name = "total") val total: Double,
)
