package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "overtime_logs")
data class OvertimeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val startTime: Long,
    val endTime: Long?,
    val duration: Double,
    val reason: String?,
    val isManual: Boolean
)
