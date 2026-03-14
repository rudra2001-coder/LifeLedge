package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalType { FINANCIAL, WORK, HEALTH, LEARNING, CUSTOM }

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val type: GoalType,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val startDate: String,
    val targetDate: String,
    val isCompleted: Boolean,
    val completedDate: String?,
    val icon: String,
    val color: Int,
    val milestones: String
)
