package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE id = :id")
    suspend fun getHabitCompletion(id: Long): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getHabitCompletionForDate(habitId: Long, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllCompletionsForHabit(habitId: Long): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE date BETWEEN :startDate AND :endDate")
    fun getCompletionsBetween(startDate: String, endDate: String): Flow<List<HabitCompletion>>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionCount(habitId: Long, startDate: String, endDate: String): Int

    @Query("""
        SELECT COUNT(*) FROM habit_completions 
        WHERE habitId = :habitId AND date <= :currentDate 
        AND date >= :startDate
    """)
    suspend fun getCurrentStreak(habitId: Long, startDate: String, currentDate: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(habitCompletion: HabitCompletion): Long

    @Update
    suspend fun updateHabitCompletion(habitCompletion: HabitCompletion)

    @Delete
    suspend fun deleteHabitCompletion(habitCompletion: HabitCompletion)
}
