package com.proteintracker.data.repository

import com.proteintracker.data.local.ProteinDao
import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.domain.toDayKey
import com.proteintracker.domain.toLocalDateOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface ProteinStore {
    suspend fun insert(entry: ProteinEntry): Long
    suspend fun update(entry: ProteinEntry)
    suspend fun delete(id: Long)
    suspend fun find(id: Long): ProteinEntry?
    fun observeDay(date: LocalDate): Flow<List<ProteinEntry>>
    fun observeTotalsIn(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Double>>
}

/**
 * Single source of truth for logged food. Converts between [LocalDate] and the
 * stored `yyyy-MM-dd` [ProteinEntry.dayKey] string here so nothing else has to.
 */
class ProteinRepository(
    private val dao: ProteinDao,
) : ProteinStore {

    override suspend fun insert(entry: ProteinEntry): Long = dao.insert(entry)

    override suspend fun update(entry: ProteinEntry) = dao.update(entry)

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun find(id: Long): ProteinEntry? = dao.findById(id)

    override fun observeDay(date: LocalDate): Flow<List<ProteinEntry>> =
        dao.observeDay(date.toDayKey())

    override fun observeTotalsIn(
        from: LocalDate,
        to: LocalDate,
    ): Flow<Map<LocalDate, Double>> =
        dao.observeTotalsBetween(from.toDayKey(), to.toDayKey())
            .map { rows -> rows.mapNotNull { it.dayKey.toLocalDateOrNull()?.let { d -> d to it.total } }.toMap() }
}
