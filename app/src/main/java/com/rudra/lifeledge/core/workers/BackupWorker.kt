package com.rudra.lifeledge.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import com.rudra.lifeledge.data.local.entity.BackupLog
import com.rudra.lifeledge.data.local.entity.BackupStatus
import com.rudra.lifeledge.data.local.entity.BackupType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val backupLog = BackupLog(
                date = backupDate,
                type = BackupType.AUTO,
                status = BackupStatus.IN_PROGRESS,
                filePath = null,
                fileSize = null,
                recordsCount = null,
                errorMessage = null
            )

            val db = LifeLedgerDatabase.getDatabase(applicationContext)
            val backupLogId = db.backupLogDao().insertBackupLog(backupLog)

            val backupDir = File(applicationContext.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val fileName = "LifeLedger_$backupDate.json"
            val backupFile = File(backupDir, fileName)

            val exportData = exportAllData(db)
            backupFile.writeText(exportData)

            val updatedLog = backupLog.copy(
                id = backupLogId,
                status = BackupStatus.SUCCESS,
                filePath = backupFile.absolutePath,
                fileSize = backupFile.length(),
                recordsCount = estimateRecordCount(db)
            )
            db.backupLogDao().updateBackupLog(updatedLog)

            cleanOldBackups(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun exportAllData(db: LifeLedgerDatabase): String {
        return buildString {
            appendLine("{")
            appendLine("  \"exportDate\": \"${LocalDate.now()}\",")
            appendLine("  \"version\": \"1.0\"")
            appendLine("}")
        }
    }

    private fun estimateRecordCount(db: LifeLedgerDatabase): Int {
        return 0
    }

    private fun cleanOldBackups(context: Context) {
        val backupDir = File(context.filesDir, "backups")
        if (backupDir.exists()) {
            val files = backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            files.drop(30).forEach { it.delete() }
        }
    }
}
