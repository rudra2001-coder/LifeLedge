package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.SettingDao
import com.rudra.lifeledge.data.local.dao.BackupLogDao
import com.rudra.lifeledge.data.local.dao.SmartAdviceLogDao
import com.rudra.lifeledge.data.local.entity.Setting
import com.rudra.lifeledge.data.local.entity.BackupLog
import com.rudra.lifeledge.data.local.entity.SmartAdviceLog
import com.rudra.lifeledge.data.local.entity.AdviceType
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingDao: SettingDao,
    private val backupLogDao: BackupLogDao,
    private val smartAdviceLogDao: SmartAdviceLogDao
) {
    fun getAllSettings(): Flow<List<Setting>> = settingDao.getAllSettings()

    suspend fun getSetting(key: String): Setting? = settingDao.getSetting(key)

    suspend fun saveSetting(setting: Setting) = settingDao.insertSetting(setting)

    suspend fun getStringSetting(key: String, default: String = ""): String {
        return settingDao.getSetting(key)?.value ?: default
    }

    suspend fun getIntSetting(key: String, default: Int = 0): Int {
        return settingDao.getSetting(key)?.value?.toIntOrNull() ?: default
    }

    suspend fun getBooleanSetting(key: String, default: Boolean = false): Boolean {
        return settingDao.getSetting(key)?.value?.toBooleanStrictOrNull() ?: default
    }

    suspend fun getDoubleSetting(key: String, default: Double = 0.0): Double {
        return settingDao.getSetting(key)?.value?.toDoubleOrNull() ?: default
    }

    fun getAllBackupLogs(): Flow<List<BackupLog>> = backupLogDao.getAllBackupLogs()

    fun getRecentBackupLogs(limit: Int): Flow<List<BackupLog>> = backupLogDao.getRecentBackupLogs(limit)

    suspend fun saveBackupLog(backupLog: BackupLog): Long = backupLogDao.insertBackupLog(backupLog)

    fun getAllSmartAdviceLogs(): Flow<List<SmartAdviceLog>> = smartAdviceLogDao.getAllSmartAdviceLogs()

    fun getSmartAdviceLogsByType(type: AdviceType): Flow<List<SmartAdviceLog>> =
        smartAdviceLogDao.getSmartAdviceLogsByType(type)

    suspend fun saveSmartAdviceLog(smartAdviceLog: SmartAdviceLog): Long =
        smartAdviceLogDao.insertSmartAdviceLog(smartAdviceLog)
}
