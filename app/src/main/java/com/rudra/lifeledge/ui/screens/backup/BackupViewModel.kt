package com.rudra.lifeledge.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.backup.BackupManager
import com.rudra.lifeledge.data.backup.BackupMetadata
import com.rudra.lifeledge.data.backup.BackupResult
import com.rudra.lifeledge.data.local.entity.Setting
import com.rudra.lifeledge.data.local.entity.SettingType
import com.rudra.lifeledge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BackupUiState(
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: String = "DAILY",
    val lastBackupDate: String? = null,
    val backupCount: Int = 0,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val message: String? = null
)

class BackupViewModel(private val settingsRepository: SettingsRepository, private val backupManager: BackupManager) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadBackupHistory()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val autoBackup = settingsRepository.getStringSetting("auto_backup_enabled", "false")
            val frequency = settingsRepository.getStringSetting("backup_frequency", "DAILY")
            val lastBackup = settingsRepository.getStringSetting("last_backup_date", "")
            val count = settingsRepository.getIntSetting("backup_count", 0)

            _uiState.value = _uiState.value.copy(
                autoBackupEnabled = autoBackup == "true",
                backupFrequency = frequency,
                lastBackupDate = lastBackup.ifBlank { null },
                backupCount = count
            )
        }
    }

    private fun loadBackupHistory() {
        val backups = backupManager.getBackupList()
        _uiState.value = _uiState.value.copy(backupCount = backups.size)
    }

    fun toggleAutoBackup() {
        val newValue = !_uiState.value.autoBackupEnabled
        _uiState.value = _uiState.value.copy(autoBackupEnabled = newValue)
        viewModelScope.launch {
            settingsRepository.saveSetting(Setting(key = "auto_backup_enabled", value = newValue.toString(), type = SettingType.STRING))
            _uiState.value = _uiState.value.copy(
                message = if (newValue) "Auto backup enabled" else "Auto backup disabled"
            )
        }
    }

    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(backupFrequency = frequency)
        viewModelScope.launch {
            settingsRepository.saveSetting(Setting(key = "backup_frequency", value = frequency, type = SettingType.STRING))
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackingUp = true, message = null)
            try {
                val result = backupManager.performBackup()
                when (result) {
                    is BackupResult.Success -> {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val now = dateFormat.format(Date())
                        settingsRepository.saveSetting(Setting(key = "last_backup_date", value = now, type = SettingType.STRING))
                        val currentCount = _uiState.value.backupCount
                        settingsRepository.saveSetting(Setting(key = "backup_count", value = (currentCount + 1).toString(), type = SettingType.STRING))
                        _uiState.value = _uiState.value.copy(
                            isBackingUp = false,
                            lastBackupDate = now,
                            backupCount = currentCount + 1,
                            message = "Backup created successfully!"
                        )
                    }
                    is BackupResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isBackingUp = false,
                            message = "Backup failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    message = "Backup failed: ${e.message}"
                )
            }
        }
    }

    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, message = null)
            try {
                val backupFile = backupManager.getBackupFile(backupId)
                if (backupFile == null) {
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        message = "Backup file not found"
                    )
                    return@launch
                }
                
                val result = backupManager.restoreFromBackup(backupFile)
                when (result) {
                    is BackupResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            message = "Data restored successfully!"
                        )
                    }
                    is BackupResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isRestoring = false,
                            message = "Restore failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    message = "Restore failed: ${e.message}"
                )
            }
        }
    }

    fun getBackupList(): List<BackupMetadata> {
        return backupManager.getBackupList()
    }
}
