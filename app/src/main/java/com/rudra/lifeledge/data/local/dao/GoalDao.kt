package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Goal
import com.rudra.lifeledge.data.local.entity.GoalType
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): Goal?

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY targetDate")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE type = :type AND isCompleted = 0")
    fun getGoalsByType(type: GoalType): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY completedDate DESC")
    fun getCompletedGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY targetDate")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}
