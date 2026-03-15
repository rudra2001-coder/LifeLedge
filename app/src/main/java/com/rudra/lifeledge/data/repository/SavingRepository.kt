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
    fun getAllGoals(): Flow<List<SavingGoal>> = savingGoalDao.getAllGoals()
    fun getActiveGoals(): Flow<List<SavingGoal>> = savingGoalDao.getActiveGoals()
    fun getCompletedGoals(): Flow<List<SavingGoal>> = savingGoalDao.getCompletedGoals()
    suspend fun getGoalById(id: Long): SavingGoal? = savingGoalDao.getGoal(id)
    suspend fun createGoal(goal: SavingGoal): Long = savingGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingGoal) = savingGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingGoal) = savingGoalDao.deleteGoal(goal)
    fun getTotalSaved(): Flow<Double?> = savingGoalDao.getTotalSaved()

    fun getAllTransactions(): Flow<List<SavingTransaction>> = savingTransactionDao.getAllTransactions()
    fun getTransactionsForGoal(goalId: Long): Flow<List<SavingTransaction>> = savingTransactionDao.getTransactionsForGoal(goalId)
    fun getRecentTransactions(limit: Int): Flow<List<SavingTransaction>> = savingTransactionDao.getAllTransactions()

    fun getTotalSavings(): Flow<Double?> = savingGoalDao.getTotalSaved()
    fun getGeneralSavingsBalance(): Flow<Double?> = savingGoalDao.getTotalSaved()

    suspend fun addSaving(amount: Double, goalId: Long?, note: String? = null, source: String = "CASH") {
        val transaction = SavingTransaction(
            amount = amount,
            goalId = goalId,
            date = System.currentTimeMillis(),
            note = note
        )
        savingTransactionDao.insertTransaction(transaction)

        if (goalId != null) {
            val goal = savingGoalDao.getGoal(goalId)
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

    suspend fun withdrawFromGoal(amount: Double, goalId: Long, note: String? = null) {
        val goal = savingGoalDao.getGoal(goalId)
        goal?.let {
            val newAmount = maxOf(0.0, it.savedAmount - amount)
            savingGoalDao.updateGoal(
                it.copy(
                    savedAmount = newAmount,
                    isCompleted = false
                )
            )
            val transaction = SavingTransaction(
                amount = -amount,
                goalId = goalId,
                date = System.currentTimeMillis(),
                note = note ?: "Withdrawal"
            )
            savingTransactionDao.insertTransaction(transaction)
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

    suspend fun saveHabit(habit: com.rudra.lifeledge.data.local.entity.Habit): Long {
        return savingGoalDao.insertGoal(habit as SavingGoal)
    }
}
