package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emi_payments")
data class EMIPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val date: String,
    val amount: Double,
    val principalPaid: Double,
    val interestPaid: Double,
    val remainingBalance: Double,
    val isPaid: Boolean
)
