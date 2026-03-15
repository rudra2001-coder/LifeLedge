package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActivityType {
    TRANSACTION,
    RECURRING_TRANSACTION,
    HABIT_COMPLETED,
    GOAL_CREATED,
    GOAL_COMPLETED,
    SAVING_ADDED,
    SAVING_WITHDRAWN,
    JOURNAL_ENTRY,
    CARD_ADDED,
    WORK_LOG
}

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: ActivityType,
    val title: String,
    val description: String?,
    val amount: Double?,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: Long?,
    val date: String,
    val color: Long = 0xFF3B82F6
)
