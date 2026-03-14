package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.WorkDay
import com.rudra.lifeledge.data.local.entity.DayType
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDayDao {
    @Query("SELECT * FROM work_calendar WHERE date = :date")
    suspend fun getWorkDay(date: String): WorkDay?

    @Query("SELECT * FROM work_calendar WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkDaysBetween(startDate: String, endDate: String): Flow<List<WorkDay>>

    @Query("SELECT * FROM work_calendar ORDER BY date DESC")
    fun getAllWorkDays(): Flow<List<WorkDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDay(workDay: WorkDay)

    @Update
    suspend fun updateWorkDay(workDay: WorkDay)

    @Delete
    suspend fun deleteWorkDay(workDay: WorkDay)

    @Query("SELECT SUM(workHours) FROM work_calendar WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalWorkHours(startDate: String, endDate: String): Double?

    @Query("SELECT SUM(overtimeHours) FROM work_calendar WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalOvertimeHours(startDate: String, endDate: String): Double?

    @Query("SELECT COUNT(*) FROM work_calendar WHERE dayType = :dayType AND date BETWEEN :startDate AND :endDate")
    suspend fun getDayTypeCount(dayType: DayType, startDate: String, endDate: String): Int
}
