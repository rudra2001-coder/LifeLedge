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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
