package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.WorkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions WHERE id = :id")
    suspend fun getWorkSession(id: Long): WorkSession?

    @Query("SELECT * FROM work_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getWorkSessionsForDate(date: String): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkSessionsBetween(startDate: String, endDate: String): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions ORDER BY date DESC")
    fun getAllWorkSessions(): Flow<List<WorkSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkSession(workSession: WorkSession): Long

    @Update
    suspend fun updateWorkSession(workSession: WorkSession)

    @Delete
    suspend fun deleteWorkSession(workSession: WorkSession)
}
