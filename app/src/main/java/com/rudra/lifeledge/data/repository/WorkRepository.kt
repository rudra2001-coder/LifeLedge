package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.WorkDayDao
import com.rudra.lifeledge.data.local.dao.OvertimeLogDao
import com.rudra.lifeledge.data.local.dao.WorkSessionDao
import com.rudra.lifeledge.data.local.dao.WorkLogDao
import com.rudra.lifeledge.data.local.entity.WorkDay
import com.rudra.lifeledge.data.local.entity.OvertimeLog
import com.rudra.lifeledge.data.local.entity.WorkSession
import com.rudra.lifeledge.data.local.entity.WorkLog
import com.rudra.lifeledge.data.local.entity.WorkType
import com.rudra.lifeledge.data.local.entity.DayType
import kotlinx.coroutines.flow.Flow

class WorkRepository(
    private val workDayDao: WorkDayDao,
    private val overtimeLogDao: OvertimeLogDao,
    private val workSessionDao: WorkSessionDao,
    private val workLogDao: WorkLogDao? = null
) {
    fun getAllWorkDays(): Flow<List<WorkDay>> = workDayDao.getAllWorkDays()

    fun getWorkDaysBetween(startDate: String, endDate: String): Flow<List<WorkDay>> =
        workDayDao.getWorkDaysBetween(startDate, endDate)

    suspend fun getWorkDay(date: String): WorkDay? = workDayDao.getWorkDay(date)

    suspend fun saveWorkDay(workDay: WorkDay) = workDayDao.insertWorkDay(workDay)

    suspend fun getTotalWorkHours(startDate: String, endDate: String): Double =
        workDayDao.getTotalWorkHours(startDate, endDate) ?: 0.0

    suspend fun getTotalOvertimeHours(startDate: String, endDate: String): Double =
        workDayDao.getTotalOvertimeHours(startDate, endDate) ?: 0.0

    suspend fun getDayTypeCount(dayType: DayType, startDate: String, endDate: String): Int =
        workDayDao.getDayTypeCount(dayType, startDate, endDate)

    fun getAllOvertimeLogs(): Flow<List<OvertimeLog>> = overtimeLogDao.getAllOvertimeLogs()

    fun getOvertimeLogsBetween(startDate: String, endDate: String): Flow<List<OvertimeLog>> =
        overtimeLogDao.getOvertimeLogsBetween(startDate, endDate)

    suspend fun saveOvertimeLog(overtimeLog: OvertimeLog): Long =
        overtimeLogDao.insertOvertimeLog(overtimeLog)

    suspend fun getTotalOvertimeDuration(startDate: String, endDate: String): Double =
        overtimeLogDao.getTotalOvertimeDuration(startDate, endDate) ?: 0.0

    fun getAllWorkSessions(): Flow<List<WorkSession>> = workSessionDao.getAllWorkSessions()

    fun getWorkSessionsForDate(date: String): Flow<List<WorkSession>> =
        workSessionDao.getWorkSessionsForDate(date)

    suspend fun saveWorkSession(workSession: WorkSession): Long =
        workSessionDao.insertWorkSession(workSession)

    // WorkLog methods (new calendar-based system)
    fun getWorkLog(date: Long): Flow<WorkLog?> = workLogDao?.getWorkLogFlow(date) ?: kotlinx.coroutines.flow.flowOf(null)

    fun getWorkLogsBetween(startDate: Long, endDate: Long): Flow<List<WorkLog>> = 
        workLogDao?.getWorkLogsBetween(startDate, endDate) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getRecentWorkLogs(limit: Int = 100): Flow<List<WorkLog>> = 
        workLogDao?.getRecentLogs(limit) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun saveWorkLog(workLog: WorkLog) = workLogDao?.insertWorkLog(workLog)

    suspend fun saveWorkLogs(workLogs: List<WorkLog>) = workLogDao?.insertWorkLogs(workLogs)

    suspend fun deleteWorkLog(date: Long) = workLogDao?.deleteWorkLogByDate(date)

    // Statistics
    suspend fun getWorkDayCount(startDate: Long, endDate: Long): Int = 
        workLogDao?.getDayTypeCount(WorkType.WORK, startDate, endDate) ?: 0

    suspend fun getOffDayCount(startDate: Long, endDate: Long): Int = 
        workLogDao?.getDayTypeCount(WorkType.OFF, startDate, endDate) ?: 0

    suspend fun getHolidayCount(startDate: Long, endDate: Long): Int = 
        workLogDao?.getDayTypeCount(WorkType.HOLIDAY, startDate, endDate) ?: 0

    suspend fun getTotalExtraHours(startDate: Long, endDate: Long): Int = 
        workLogDao?.getTotalExtraHours(startDate, endDate) ?: 0

    suspend fun getTotalExtraHoursAllTime(): Int = 
        workLogDao?.getTotalExtraHoursAllTime() ?: 0

    fun getWorkDaysFlow(): Flow<List<WorkLog>> = 
        workLogDao?.getWorkDays() ?: kotlinx.coroutines.flow.flowOf(emptyList())
}
