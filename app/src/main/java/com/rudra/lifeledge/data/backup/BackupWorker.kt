package com.rudra.lifeledge.data.backup

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val backupManager = BackupManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val result = backupManager.performBackup()
            when (result) {
                is BackupResult.Success -> Result.success()
                is BackupResult.Error -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val BACKUP_WORK_NAME = "daily_backup"

        fun scheduleDailyBackup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.HOURS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
        }

        fun cancelScheduledBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
        }

        fun scheduleImmediateBackup(context: Context) {
            val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(backupRequest)
        }
    }
}
