package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.ActivityLog
import com.rudra.lifeledge.data.local.entity.ActivityType
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getActivityLogs(limit: Int = 100, offset: Int = 0): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getActivityLogsForDate(date: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getActivityLogsBetween(startDate: String, endDate: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE type = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getActivityLogsByType(type: ActivityType, limit: Int = 50): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    fun searchActivityLogs(query: String, limit: Int = 50): Flow<List<ActivityLog>>

    @Query("SELECT COUNT(*) FROM activity_logs WHERE date = :date")
    fun getActivityCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM activity_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getActivityCountBetween(startDate: String, endDate: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(activityLog: ActivityLog): Long

    @Delete
    suspend fun deleteActivityLog(activityLog: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}
