package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val frequency: Frequency,
    val interval: Int,
    val executeDay: Int?,
    val startDate: String,
    val endDate: String?,
    val nextDate: String,
    val lastExecutedDate: String?,
    val payee: String?,
    val notes: String?,
    val isActive: Boolean
)
