package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.WorkDayDao
import com.rudra.lifeledge.data.local.dao.OvertimeLogDao
import com.rudra.lifeledge.data.local.dao.WorkSessionDao
import com.rudra.lifeledge.data.local.entity.WorkDay
import com.rudra.lifeledge.data.local.entity.OvertimeLog
import com.rudra.lifeledge.data.local.entity.WorkSession
import com.rudra.lifeledge.data.local.entity.DayType
import kotlinx.coroutines.flow.Flow

class WorkRepository(
    private val workDayDao: WorkDayDao,
    private val overtimeLogDao: OvertimeLogDao,
    private val workSessionDao: WorkSessionDao
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
}
