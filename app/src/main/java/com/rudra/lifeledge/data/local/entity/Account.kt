package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { CASH, BANK, MOBILE_BANKING, CREDIT_CARD, SAVINGS }

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String,
    val icon: String?,
    val isActive: Boolean,
    val notes: String?
)
