package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey
    val date: String,
    val mood: Mood,
    val energy: Int,
    val productivity: Int,
    val sleepHours: Double,
    val sleepQuality: Int,
    val waterIntake: Int,
    val steps: Int,
    val exerciseMinutes: Int,
    val meditationMinutes: Int,
    val notes: String?,
    val gratitude1: String?,
    val gratitude2: String?,
    val gratitude3: String?
)
