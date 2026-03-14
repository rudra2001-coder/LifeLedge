package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BackupType { AUTO, MANUAL }
enum class BackupStatus { SUCCESS, FAILED, IN_PROGRESS }

@Entity(tableName = "backup_logs")
data class BackupLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val type: BackupType,
    val status: BackupStatus,
    val filePath: String?,
    val fileSize: Long?,
    val recordsCount: Int?,
    val errorMessage: String?
)
