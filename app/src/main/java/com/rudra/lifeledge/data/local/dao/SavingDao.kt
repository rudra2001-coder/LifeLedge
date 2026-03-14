package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.SavingGoal
import com.rudra.lifeledge.data.local.entity.SavingTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals WHERE isCompleted = 0 ORDER BY createdDate DESC")
    fun getActiveGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE isCompleted = 1 ORDER BY createdDate DESC")
    fun getCompletedGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingGoal)

    @Query("SELECT SUM(savedAmount) FROM saving_goals")
    fun getTotalSavedFromGoals(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM saving_goals WHERE isCompleted = 0")
    fun getActiveGoalsCount(): Flow<Int>
}

@Dao
interface SavingTransactionDao {
    @Query("SELECT * FROM saving_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE goalId = :goalId ORDER BY date DESC")
    fun getTransactionsByGoal(goalId: Long): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE goalId IS NULL ORDER BY date DESC")
    fun getGeneralSavingsTransactions(): Flow<List<SavingTransaction>>

    @Query("SELECT SUM(amount) FROM saving_transactions")
    fun getTotalSavings(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM saving_transactions WHERE goalId IS NULL")
    fun getGeneralSavingsBalance(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM saving_transactions WHERE date >= :startDate AND date <= :endDate")
    fun getSavingsInPeriod(startDate: Long, endDate: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SavingTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: SavingTransaction)

    @Query("SELECT * FROM saving_transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<SavingTransaction>>
}
