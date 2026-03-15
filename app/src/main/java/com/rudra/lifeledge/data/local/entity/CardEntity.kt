package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CardType {
    DEBIT_CARD,
    CREDIT_CARD,
    CASH_WALLET,
    MOBILE_WALLET
}

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bankName: String,
    val cardType: CardType,
    val balance: Double,
    val color: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
