package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.OvertimeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeLogDao {
    @Query("SELECT * FROM overtime_logs WHERE id = :id")
    suspend fun getOvertimeLog(id: Long): OvertimeLog?

    @Query("SELECT * FROM overtime_logs WHERE date = :date ORDER BY startTime DESC")
    fun getOvertimeLogsForDate(date: String): Flow<List<OvertimeLog>>

    @Query("SELECT * FROM overtime_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getOvertimeLogsBetween(startDate: String, endDate: String): Flow<List<OvertimeLog>>

    @Query("SELECT * FROM overtime_logs ORDER BY date DESC")
    fun getAllOvertimeLogs(): Flow<List<OvertimeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertimeLog(overtimeLog: OvertimeLog): Long

    @Update
    suspend fun updateOvertimeLog(overtimeLog: OvertimeLog)

    @Delete
    suspend fun deleteOvertimeLog(overtimeLog: OvertimeLog)

    @Query("SELECT SUM(duration) FROM overtime_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalOvertimeDuration(startDate: String, endDate: String): Double?
}
