package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.WorkLog
import com.rudra.lifeledge.data.local.entity.WorkType
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs WHERE date = :date")
    suspend fun getWorkLog(date: Long): WorkLog?

    @Query("SELECT * FROM work_logs WHERE date = :date")
    fun getWorkLogFlow(date: Long): Flow<WorkLog?>

    @Query("SELECT * FROM work_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkLogsBetween(startDate: Long, endDate: Long): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs ORDER BY date DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs ORDER BY date DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLogs(workLogs: List<WorkLog>)

    @Update
    suspend fun updateWorkLog(workLog: WorkLog)

    @Delete
    suspend fun deleteWorkLog(workLog: WorkLog)

    @Query("DELETE FROM work_logs WHERE date = :date")
    suspend fun deleteWorkLogByDate(date: Long)

    // Statistics queries
    @Query("SELECT COUNT(*) FROM work_logs WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getDayTypeCount(type: WorkType, startDate: Long, endDate: Long): Int

    @Query("SELECT SUM(extraHours) FROM work_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExtraHours(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(extraHours) FROM work_logs")
    suspend fun getTotalExtraHoursAllTime(): Int?

    // Get work streak
    @Query("SELECT * FROM work_logs WHERE type = 'WORK' ORDER BY date DESC")
    fun getWorkDays(): Flow<List<WorkLog>>

    // Check if date exists
    @Query("SELECT EXISTS(SELECT 1 FROM work_logs WHERE date = :date)")
    suspend fun hasWorkLog(date: Long): Boolean
}
