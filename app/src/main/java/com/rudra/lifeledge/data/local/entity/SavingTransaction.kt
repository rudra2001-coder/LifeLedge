package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_transactions")
data class SavingTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val goalId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null
)
