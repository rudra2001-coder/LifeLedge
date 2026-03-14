package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AdviceType { EXPENSE, WORK, HABIT, GOAL, BURNOUT }

@Entity(tableName = "smart_advice_logs")
data class SmartAdviceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val type: AdviceType,
    val message: String,
    val actionTaken: Boolean,
    val userResponse: String?
)
