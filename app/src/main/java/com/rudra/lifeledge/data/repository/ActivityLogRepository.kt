package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.ActivityLogDao
import com.rudra.lifeledge.data.local.entity.ActivityLog
import com.rudra.lifeledge.data.local.entity.ActivityType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActivityLogRepository(
    private val activityLogDao: ActivityLogDao
) {
    fun getActivityLogs(limit: Int = 100, offset: Int = 0): Flow<List<ActivityLog>> =
        activityLogDao.getActivityLogs(limit, offset)

    fun getActivityLogsForDate(date: String): Flow<List<ActivityLog>> =
        activityLogDao.getActivityLogsForDate(date)

    fun getActivityLogsBetween(startDate: String, endDate: String): Flow<List<ActivityLog>> =
        activityLogDao.getActivityLogsBetween(startDate, endDate)

    fun getActivityLogsByType(type: ActivityType, limit: Int = 50): Flow<List<ActivityLog>> =
        activityLogDao.getActivityLogsByType(type, limit)

    fun searchActivityLogs(query: String, limit: Int = 50): Flow<List<ActivityLog>> =
        activityLogDao.searchActivityLogs(query, limit)

    fun getActivityCountForDate(date: String): Flow<Int> =
        activityLogDao.getActivityCountForDate(date)

    fun getActivityCountBetween(startDate: String, endDate: String): Flow<Int> =
        activityLogDao.getActivityCountBetween(startDate, endDate)

    suspend fun logActivity(
        type: ActivityType,
        title: String,
        description: String? = null,
        amount: Double? = null,
        referenceId: Long? = null,
        color: Long = 0xFF3B82F6
    ) {
        val log = ActivityLog(
            type = type,
            title = title,
            description = description,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            referenceId = referenceId,
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            color = color
        )
        activityLogDao.insertActivityLog(log)
    }

    suspend fun deleteActivityLog(activityLog: ActivityLog) =
        activityLogDao.deleteActivityLog(activityLog)

    suspend fun deleteOldLogs(olderThanDays: Int = 90) {
        val cutoffDate = LocalDate.now().minusDays(olderThanDays.toLong())
        activityLogDao.deleteOldLogs(cutoffDate.toEpochDay() * 86400000)
    }
}
