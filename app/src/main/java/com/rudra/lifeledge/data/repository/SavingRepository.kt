package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.SavingGoalDao
import com.rudra.lifeledge.data.local.dao.SavingTransactionDao
import com.rudra.lifeledge.data.local.entity.SavingGoal
import com.rudra.lifeledge.data.local.entity.SavingTransaction
import kotlinx.coroutines.flow.Flow

class SavingRepository(
    private val savingGoalDao: SavingGoalDao,
    private val savingTransactionDao: SavingTransactionDao
) {
    // Goals
    fun getActiveGoals(): Flow<List<SavingGoal>> = savingGoalDao.getActiveGoals()
    fun getCompletedGoals(): Flow<List<SavingGoal>> = savingGoalDao.getCompletedGoals()
    suspend fun getGoalById(id: Long): SavingGoal? = savingGoalDao.getGoalById(id)
    suspend fun createGoal(goal: SavingGoal): Long = savingGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingGoal) = savingGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingGoal) = savingGoalDao.deleteGoal(goal)
    fun getActiveGoalsCount(): Flow<Int> = savingGoalDao.getActiveGoalsCount()

    // Transactions
    fun getAllTransactions(): Flow<List<SavingTransaction>> = savingTransactionDao.getAllTransactions()
    fun getTransactionsByGoal(goalId: Long): Flow<List<SavingTransaction>> = savingTransactionDao.getTransactionsByGoal(goalId)
    fun getGeneralSavingsTransactions(): Flow<List<SavingTransaction>> = savingTransactionDao.getGeneralSavingsTransactions()
    fun getRecentTransactions(limit: Int): Flow<List<SavingTransaction>> = savingTransactionDao.getRecentTransactions(limit)

    // Balance
    fun getTotalSavings(): Flow<Double?> = savingTransactionDao.getTotalSavings()
    fun getGeneralSavingsBalance(): Flow<Double?> = savingTransactionDao.getGeneralSavingsBalance()
    fun getSavingsInPeriod(startDate: Long, endDate: Long): Flow<Double?> = savingTransactionDao.getSavingsInPeriod(startDate, endDate)

    // Add Saving Logic
    suspend fun addSaving(amount: Double, goalId: Long?, note: String? = null) {
        val transaction = SavingTransaction(
            amount = amount,
            goalId = goalId,
            date = System.currentTimeMillis(),
            note = note
        )
        savingTransactionDao.insertTransaction(transaction)

        if (goalId != null) {
            val goal = savingGoalDao.getGoalById(goalId)
            goal?.let {
                val newAmount = it.savedAmount + amount
                val isCompleted = newAmount >= it.targetAmount
                savingGoalDao.updateGoal(
                    it.copy(
                        savedAmount = newAmount,
                        isCompleted = isCompleted
                    )
                )
            }
        }
    }

    suspend fun addGoal(title: String, targetAmount: Double, priority: String = "MEDIUM", icon: String = "🎯", color: Long = 0xFF3B82F6): Long {
        val goal = SavingGoal(
            title = title,
            targetAmount = targetAmount,
            priority = priority,
            icon = icon,
            color = color
        )
        return savingGoalDao.insertGoal(goal)
    }
}
