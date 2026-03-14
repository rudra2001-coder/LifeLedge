package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val weight: Double?,
    val bodyFat: Double?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?,
    val heartRate: Int?,
    val bloodSugar: Double?,
    val notes: String?
)
