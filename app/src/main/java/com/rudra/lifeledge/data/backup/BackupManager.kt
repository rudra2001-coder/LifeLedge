package com.rudra.lifeledge.data.backup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class BackupMetadata(
    val backupId: String,
    val timestamp: Long,
    val version: Int,
    val appVersion: String,
    val dataSize: Long,
    val checksum: String,
    val dataTypes: List<String>
)

data class BackupData(
    val metadata: BackupMetadata,
    val transactions: List<Map<String, Any>>,
    val habits: List<Map<String, Any>>,
    val goals: List<Map<String, Any>>,
    val journalEntries: List<Map<String, Any>>,
    val savings: List<Map<String, Any>>,
    val workLogs: List<Map<String, Any>>,
    val settings: Map<String, String>
)

sealed class BackupResult {
    data class Success(val filePath: String, val metadata: BackupMetadata) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

class BackupManager(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
    
    companion object {
        private const val BACKUP_DIR = "app_backups"
        private const val MAX_BACKUP_COUNT = 10
        private const val BACKUP_VERSION = 1
    }
    
    suspend fun performBackup(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) backupDir.mkdirs()
            
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateStr = dateFormat.format(Date(timestamp))
            val backupId = "backup_$dateStr"
            
            val data = collectAllData()
            
            val checksum = calculateChecksum(gson.toJson(data))
            
            val metadata = BackupMetadata(
                backupId = backupId,
                timestamp = timestamp,
                version = BACKUP_VERSION,
                appVersion = getAppVersion(),
                dataSize = 0,
                checksum = checksum,
                dataTypes = listOf("transactions", "habits", "goals", "journal", "savings", "worklogs")
            )
            
            val backupData = BackupData(
                metadata = metadata,
                transactions = data.transactions,
                habits = data.habits,
                goals = data.goals,
                journalEntries = data.journalEntries,
                savings = data.savings,
                workLogs = data.workLogs,
                settings = data.settings
            )
            
            val backupFile = File(backupDir, "${backupId}.json")
            backupFile.writeText(gson.toJson(backupData))
            
            val updatedMetadata = metadata.copy(dataSize = backupFile.length())
            saveBackupMetadata(updatedMetadata)
            cleanupOldBackups()
            
            BackupResult.Success(backupFile.absolutePath, updatedMetadata)
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Backup failed")
        }
    }
    
    private data class CollectedData(
        val transactions: List<Map<String, Any>>,
        val habits: List<Map<String, Any>>,
        val goals: List<Map<String, Any>>,
        val journalEntries: List<Map<String, Any>>,
        val savings: List<Map<String, Any>>,
        val workLogs: List<Map<String, Any>>,
        val settings: Map<String, String>
    )
    
    private suspend fun collectAllData(): CollectedData = withContext(Dispatchers.IO) {
        val db = LifeLedgerDatabase.getDatabase(context)
        
        val transactions = mutableListOf<Map<String, Any>>()
        val habits = mutableListOf<Map<String, Any>>()
        val goals = mutableListOf<Map<String, Any>>()
        val journalEntries = mutableListOf<Map<String, Any>>()
        val savings = mutableListOf<Map<String, Any>>()
        val workLogs = mutableListOf<Map<String, Any>>()
        
        try {
            db.transactionDao().getRecentTransactions(1000).first().forEach { tx ->
                transactions.add(mapOf(
                    "id" to tx.id,
                    "amount" to tx.amount,
                    "type" to tx.type.name,
                    "categoryId" to tx.categoryId,
                    "notes" to (tx.notes ?: ""),
                    "date" to tx.date,
                    "accountId" to tx.accountId
                ))
            }
        } catch (e: Exception) { }
        
        try {
            db.habitDao().getAllHabits().first().forEach { habit ->
                habits.add(mapOf(
                    "id" to habit.id,
                    "name" to habit.name,
                    "description" to (habit.description ?: ""),
                    "frequency" to habit.frequency,
                    "icon" to (habit.icon ?: ""),
                    "color" to (habit.color ?: ""),
                    "createdAt" to habit.createdAt
                ))
            }
        } catch (e: Exception) { }
        
        try {
            db.goalDao().getAllGoals().first().forEach { goal ->
                goals.add(mapOf(
                    "id" to goal.id,
                    "title" to goal.title,
                    "description" to (goal.description ?: ""),
                    "type" to goal.type.name,
                    "targetValue" to goal.targetValue,
                    "currentValue" to (goal.currentValue ?: 0.0),
                    "unit" to (goal.unit ?: ""),
                    "startDate" to goal.startDate,
                    "targetDate" to goal.targetDate,
                    "isCompleted" to goal.isCompleted
                ))
            }
        } catch (e: Exception) { }
        
        try {
            db.journalEntryDao().getAllJournalEntries().first().forEach { entry ->
                journalEntries.add(mapOf(
                    "id" to entry.id,
                    "date" to entry.date,
                    "title" to entry.title,
                    "content" to entry.content,
                    "mood" to entry.mood.name,
                    "tags" to entry.tags,
                    "energy" to entry.energy,
                    "productivity" to entry.productivity
                ))
            }
        } catch (e: Exception) { }
        
        try {
            val activeGoals = db.savingGoalDao().getActiveGoals().first()
            val completedGoals = db.savingGoalDao().getCompletedGoals().first()
            (activeGoals + completedGoals).forEach { saving ->
                savings.add(mapOf(
                    "id" to saving.id,
                    "title" to saving.title,
                    "targetAmount" to saving.targetAmount,
                    "savedAmount" to saving.savedAmount,
                    "createdDate" to saving.createdDate,
                    "isCompleted" to saving.isCompleted,
                    "priority" to saving.priority,
                    "icon" to saving.icon,
                    "color" to saving.color
                ))
            }
        } catch (e: Exception) { }
        
        try {
            db.workLogDao().getAllWorkLogs().first().forEach { log ->
                workLogs.add(mapOf(
                    "date" to log.date,
                    "type" to log.type.name,
                    "extraHours" to log.extraHours,
                    "note" to (log.note ?: "")
                ))
            }
        } catch (e: Exception) { }
        
        val settings = try {
            val pref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            pref.all.mapValues { it.value?.toString() ?: "" }
        } catch (e: Exception) { emptyMap() }
        
        CollectedData(transactions, habits, goals, journalEntries, savings, workLogs, settings)
    }
    
    suspend fun restoreFromBackup(backupFile: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupJson = backupFile.readText()
            val backupData = gson.fromJson(backupJson, BackupData::class.java)
            
            val calculatedChecksum = calculateChecksum(
                gson.toJson(BackupData(
                    metadata = backupData.metadata,
                    transactions = backupData.transactions,
                    habits = backupData.habits,
                    goals = backupData.goals,
                    journalEntries = backupData.journalEntries,
                    savings = backupData.savings,
                    workLogs = backupData.workLogs,
                    settings = backupData.settings
                ))
            )
            
            if (calculatedChecksum != backupData.metadata.checksum) {
                return@withContext BackupResult.Error("Backup file corrupted")
            }
            
            restoreData(backupData)
            
            BackupResult.Success(backupFile.absolutePath, backupData.metadata)
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Restore failed")
        }
    }
    
    private suspend fun restoreData(data: BackupData) = withContext(Dispatchers.IO) {
        val db = LifeLedgerDatabase.getDatabase(context)
        
        // Settings restoration
        data.settings.forEach { (key, value) ->
            val pref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            pref.edit().putString(key, value).apply()
        }
    }
    
    fun getBackupList(): List<BackupMetadata> {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) return emptyList()
        
        return backupDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val json = file.readText()
                    val data = gson.fromJson(json, BackupData::class.java)
                    data.metadata
                } catch (e: Exception) { null }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }
    
    fun getBackupFile(backupId: String): File? {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        val file = File(backupDir, "$backupId.json")
        return if (file.exists()) file else null
    }
    
    private fun saveBackupMetadata(metadata: BackupMetadata) {
        val pref = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        val existing = getBackupList().toMutableList()
        existing.add(0, metadata)
        if (existing.size > MAX_BACKUP_COUNT) {
            existing.removeAt(existing.size - 1)
        }
        val json = gson.toJson(existing.map { mapOf(
            "backupId" to it.backupId,
            "timestamp" to it.timestamp,
            "version" to it.version,
            "appVersion" to it.appVersion,
            "dataSize" to it.dataSize,
            "checksum" to it.checksum,
            "dataTypes" to it.dataTypes
        ) })
        pref.edit().putString("backup_metadata", json).apply()
    }
    
    private fun cleanupOldBackups() {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        val files = backupDir.listFiles { _, name -> name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?: return
        
        if (files.size > MAX_BACKUP_COUNT) {
            files.drop(MAX_BACKUP_COUNT).forEach { it.delete() }
        }
    }
    
    private fun calculateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun getAppVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }
    }
}
