package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DayType { WORK, HOME_OFFICE, OFF, HOLIDAY, SICK }

@Entity(tableName = "work_calendar")
data class WorkDay(
    @PrimaryKey
    val date: String,
    val dayType: DayType,
    val workHours: Double,
    val overtimeHours: Double,
    val startTime: String?,
    val endTime: String?,
    val notes: String?,
    val lastUpdated: Long
)
