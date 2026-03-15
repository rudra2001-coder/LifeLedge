package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(val categoryId: Long, val total: Double)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY id DESC")
    fun getTransactionsForDate(date: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetween(startDate: String, endDate: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate AND type = 'EXPENSE'
        GROUP BY categoryId
    """)
    fun getExpensesByCategory(startDate: String, endDate: String): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByType(type: TransactionType, startDate: String, endDate: String): Double?

    // Balance Calculation Queries
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'SAVE'")
    fun getTotalSaved(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'TRANSFER_FROM_SAVING'")
    fun getTotalTransferredFromSavings(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'SAVE' - COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'TRANSFER_FROM_SAVING'), 0)")
    fun getSavingsBalance(): Flow<Double>

    // Net Balance = Income - Expense - Saved + TransferredFromSavings
    @Query("""
        SELECT COALESCE(SUM(CASE 
            WHEN type = 'INCOME' THEN amount 
            WHEN type = 'EXPENSE' THEN -amount 
            WHEN type = 'SAVE' THEN -amount 
            WHEN type = 'TRANSFER_FROM_SAVING' THEN amount 
            ELSE 0 END), 0) 
        FROM transactions
    """)
    fun getNetBalance(): Flow<Double>

    // Monthly totals
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getMonthlyIncome(startDate: String, endDate: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getMonthlyExpense(startDate: String, endDate: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'SAVE' AND date BETWEEN :startDate AND :endDate")
    fun getMonthlySaved(startDate: String, endDate: String): Flow<Double>

    // Daily totals
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date = :date")
    fun getDailyIncome(date: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date = :date")
    fun getDailyExpense(date: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
