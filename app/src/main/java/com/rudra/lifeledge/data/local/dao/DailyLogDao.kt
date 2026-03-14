package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getDailyLog(date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailyLogsBetween(startDate: String, endDate: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDailyLogs(): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(dailyLog: DailyLog)

    @Update
    suspend fun updateDailyLog(dailyLog: DailyLog)

    @Delete
    suspend fun deleteDailyLog(dailyLog: DailyLog)
}
