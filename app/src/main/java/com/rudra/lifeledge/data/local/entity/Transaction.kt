package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { 
    INCOME, 
    EXPENSE, 
    SAVE,
    TRANSFER_FROM_SAVING,
    TRANSFER 
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long?,
    val payee: String?,
    val notes: String?,
    val isRecurring: Boolean,
    val recurringId: Long?,
    val isCleared: Boolean,
    val attachment: String?,
    val location: String?,
    val tags: String
)
