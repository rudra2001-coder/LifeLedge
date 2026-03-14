package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.JournalEntryDao
import com.rudra.lifeledge.data.local.dao.DailyLogDao
import com.rudra.lifeledge.data.local.entity.JournalEntry
import com.rudra.lifeledge.data.local.entity.DailyLog
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalEntryDao: JournalEntryDao,
    private val dailyLogDao: DailyLogDao
) {
    fun getAllJournalEntries(): Flow<List<JournalEntry>> = journalEntryDao.getAllJournalEntries()

    fun getJournalEntriesForDate(date: String): Flow<List<JournalEntry>> =
        journalEntryDao.getJournalEntriesForDate(date)

    fun getJournalEntriesBetween(startDate: String, endDate: String): Flow<List<JournalEntry>> =
        journalEntryDao.getJournalEntriesBetween(startDate, endDate)

    fun getFavoriteJournalEntries(): Flow<List<JournalEntry>> =
        journalEntryDao.getFavoriteJournalEntries()

    fun searchJournalEntries(query: String): Flow<List<JournalEntry>> =
        journalEntryDao.searchJournalEntries(query)

    suspend fun getJournalEntry(id: Long): JournalEntry? = journalEntryDao.getJournalEntry(id)

    suspend fun saveJournalEntry(journalEntry: JournalEntry): Long =
        journalEntryDao.insertJournalEntry(journalEntry)

    suspend fun deleteJournalEntry(journalEntry: JournalEntry) =
        journalEntryDao.deleteJournalEntry(journalEntry)

    fun getAllDailyLogs(): Flow<List<DailyLog>> = dailyLogDao.getAllDailyLogs()

    fun getDailyLogsBetween(startDate: String, endDate: String): Flow<List<DailyLog>> =
        dailyLogDao.getDailyLogsBetween(startDate, endDate)

    suspend fun getDailyLog(date: String): DailyLog? = dailyLogDao.getDailyLog(date)

    suspend fun saveDailyLog(dailyLog: DailyLog) = dailyLogDao.insertDailyLog(dailyLog)
}
