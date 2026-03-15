package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.SavingGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals ORDER BY isCompleted ASC, createdDate DESC")
    fun getAllGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE isCompleted = 0 ORDER BY createdDate DESC")
    fun getActiveGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE isCompleted = 1 ORDER BY createdDate DESC")
    fun getCompletedGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE id = :id")
    suspend fun getGoal(id: Long): SavingGoal?

    @Query("SELECT SUM(savedAmount) FROM saving_goals")
    fun getTotalSaved(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingGoal)

    @Query("UPDATE saving_goals SET savedAmount = savedAmount + :amount WHERE id = :goalId")
    suspend fun addToGoal(goalId: Long, amount: Double)

    @Query("UPDATE saving_goals SET savedAmount = savedAmount - :amount WHERE id = :goalId")
    suspend fun withdrawFromGoal(goalId: Long, amount: Double)

    @Query("UPDATE saving_goals SET isCompleted = :isCompleted WHERE id = :goalId")
    suspend fun setCompleted(goalId: Long, isCompleted: Boolean)
}
