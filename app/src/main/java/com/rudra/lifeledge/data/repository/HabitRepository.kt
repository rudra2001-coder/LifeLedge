package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.HabitDao
import com.rudra.lifeledge.data.local.dao.HabitCompletionDao
import com.rudra.lifeledge.data.local.entity.Habit
import com.rudra.lifeledge.data.local.entity.HabitCompletion
import com.rudra.lifeledge.data.local.entity.HabitCategory
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()

    fun getHabitsByCategory(category: HabitCategory): Flow<List<Habit>> =
        habitDao.getHabitsByCategory(category)

    suspend fun getHabit(id: Long): Habit? = habitDao.getHabit(id)

    suspend fun saveHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> =
        habitCompletionDao.getCompletionsForDate(date)

    fun getCompletionsBetween(startDate: String, endDate: String): Flow<List<HabitCompletion>> =
        habitCompletionDao.getCompletionsBetween(startDate, endDate)

    suspend fun getCompletionForDate(habitId: Long, date: String): HabitCompletion? =
        habitCompletionDao.getHabitCompletionForDate(habitId, date)

    suspend fun saveHabitCompletion(habitCompletion: HabitCompletion): Long =
        habitCompletionDao.insertHabitCompletion(habitCompletion)

    suspend fun deleteHabitCompletion(habitCompletion: HabitCompletion) =
        habitCompletionDao.deleteHabitCompletion(habitCompletion)

    suspend fun getCurrentStreak(habitId: Long, startDate: String, currentDate: String): Int =
        habitCompletionDao.getCurrentStreak(habitId, startDate, currentDate)

    suspend fun getCompletionCount(habitId: Long, startDate: String, endDate: String): Int =
        habitCompletionDao.getCompletionCount(habitId, startDate, endDate)

    suspend fun getAllCompletionsForHabit(habitId: Long): List<HabitCompletion> =
        habitCompletionDao.getAllCompletionsForHabit(habitId)
}
