package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_goals")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM",
    val icon: String = "🎯",
    val color: Long = 0xFF3B82F6
)
