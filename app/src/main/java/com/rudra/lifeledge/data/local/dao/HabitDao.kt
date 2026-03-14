package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Habit
import com.rudra.lifeledge.data.local.entity.HabitCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY name")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE category = :category AND isActive = 1")
    fun getHabitsByCategory(category: HabitCategory): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
}
