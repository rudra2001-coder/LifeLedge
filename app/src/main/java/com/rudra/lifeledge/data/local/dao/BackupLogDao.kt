package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.BackupLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupLogDao {
    @Query("SELECT * FROM backup_logs WHERE id = :id")
    suspend fun getBackupLog(id: Long): BackupLog?

    @Query("SELECT * FROM backup_logs ORDER BY date DESC")
    fun getAllBackupLogs(): Flow<List<BackupLog>>

    @Query("SELECT * FROM backup_logs ORDER BY date DESC LIMIT :limit")
    fun getRecentBackupLogs(limit: Int): Flow<List<BackupLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupLog(backupLog: BackupLog): Long

    @Update
    suspend fun updateBackupLog(backupLog: BackupLog)

    @Delete
    suspend fun deleteBackupLog(backupLog: BackupLog)
}
