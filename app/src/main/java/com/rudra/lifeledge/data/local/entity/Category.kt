package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: String,
    val color: Int,
    val parentId: Long?,
    val budget: Double?,
    val isActive: Boolean
)
