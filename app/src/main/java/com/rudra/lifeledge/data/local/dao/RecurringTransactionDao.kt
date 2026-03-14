package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransaction(id: Long): RecurringTransaction?

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDate")
    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions ORDER BY nextDate")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
}
