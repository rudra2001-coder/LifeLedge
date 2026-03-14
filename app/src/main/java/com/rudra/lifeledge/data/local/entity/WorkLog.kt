package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class WorkType {
    WORK,
    OFF,
    HOLIDAY
}

@Entity(
    tableName = "work_logs",
    indices = [Index(value = ["date"])]
)
data class WorkLog(
    @PrimaryKey
    val date: Long,  // Date in millis (start of day)
    val type: WorkType = WorkType.WORK,
    val extraHours: Int = 0,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
