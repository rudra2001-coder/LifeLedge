package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cardNumber: String,
    val bankName: String,
    val creditLimit: Double,
    val availableCredit: Double,
    val billingDate: Int,
    val dueDate: Int,
    val interestRate: Double,
    val notes: String?
)
