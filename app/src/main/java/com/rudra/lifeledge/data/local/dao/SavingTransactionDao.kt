package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.SavingTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingTransactionDao {
    @Query("SELECT * FROM saving_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE goalId = :goalId ORDER BY date DESC")
    fun getTransactionsForGoal(goalId: Long): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): SavingTransaction?

    @Query("SELECT SUM(amount) FROM saving_transactions WHERE goalId = :goalId")
    fun getTotalForGoal(goalId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SavingTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: SavingTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: SavingTransaction)
}
