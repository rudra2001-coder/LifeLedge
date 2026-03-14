package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getJournalEntry(id: Long): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE date = :date")
    fun getJournalEntriesForDate(date: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getJournalEntriesBetween(startDate: String, endDate: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoriteJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchJournalEntries(query: String): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(journalEntry: JournalEntry): Long

    @Update
    suspend fun updateJournalEntry(journalEntry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(journalEntry: JournalEntry)
}
