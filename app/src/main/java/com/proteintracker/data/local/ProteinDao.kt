package com.proteintracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProteinDao {

    @Insert
    suspend fun insert(entry: ProteinEntry): Long

    @Update
    suspend fun update(entry: ProteinEntry)

    @Query("DELETE FROM protein_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM protein_entries WHERE dayKey = :dayKey ORDER BY loggedAt DESC")
    fun observeDay(dayKey: String): Flow<List<ProteinEntry>>

    @Query("SELECT * FROM protein_entries WHERE id = :id")
    suspend fun findById(id: Long): ProteinEntry?

    /**
     * `dayKey` is a zero-padded `yyyy-MM-dd` string, so a lexicographic
     * `BETWEEN` is also a chronological range scan.
     */
    @Query(
        """
        SELECT dayKey, SUM(totalProtein) AS total
        FROM protein_entries
        WHERE dayKey BETWEEN :fromDayKey AND :toDayKey
        GROUP BY dayKey
        """
    )
    fun observeTotalsBetween(fromDayKey: String, toDayKey: String): Flow<List<DayTotal>>
}
