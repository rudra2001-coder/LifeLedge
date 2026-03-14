package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val startTime: Long,
    val endTime: Long?,
    val task: String,
    val project: String?,
    val tags: String,
    val notes: String?
)
