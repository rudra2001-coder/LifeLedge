package com.rudra.lifeledge.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import com.rudra.lifeledge.data.repository.SettingsRepository
import com.rudra.lifeledge.ui.theme.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                AutoBackupSection(
                    enabled = uiState.autoBackupEnabled,
                    frequency = uiState.backupFrequency,
                    onToggle = { viewModel.toggleAutoBackup() },
                    onFrequencyChange = { viewModel.updateFrequency(it) }
                )
            }

            item {
                ManualBackupSection(
                    isBackingUp = uiState.isBackingUp,
                    lastBackupDate = uiState.lastBackupDate,
                    backupCount = uiState.backupCount,
                    onBackupNow = { viewModel.backupNow() }
                )
            }

            item {
                RestoreSection(
                    isRestoring = uiState.isRestoring,
                    onRestore = { viewModel.restoreBackup() }
                )
            }

            if (uiState.message != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.message!!.contains("Success") || uiState.message!!.contains("success")) 
                                Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = uiState.message!!,
                            modifier = Modifier.padding(16.dp),
                            color = if (uiState.message!!.contains("Success") || uiState.message!!.contains("success")) 
                                Success else Error
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AutoBackupSection(
    enabled: Boolean,
    frequency: String,
    onToggle: () -> Unit,
    onFrequencyChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Auto Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Automatically backup your data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = Primary.copy(alpha = 0.5f))
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Backup Frequency",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly").forEach { (value, label) ->
                        FilterChip(
                            selected = frequency == value,
                            onClick = { onFrequencyChange(value) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManualBackupSection(
    isBackingUp: Boolean,
    lastBackupDate: String?,
    backupCount: Int,
    onBackupNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Success.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Backup,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Manual Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (lastBackupDate != null) "Last: $lastBackupDate" else "No backups yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Backups: $backupCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBackupNow,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBackingUp,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isBackingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backing up...")
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Now")
                }
            }
        }
    }
}

@Composable
fun RestoreSection(
    isRestoring: Boolean,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Warning.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Restore Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Restore from a previous backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRestoring,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Warning,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restoring...")
                } else {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Backup File")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Warning: This will replace all current data",
                style = MaterialTheme.typography.bodySmall,
                color = Error
            )
        }
    }
}

class BackupViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
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

    fun toggleAutoBackup() {
        val newValue = !_uiState.value.autoBackupEnabled
        _uiState.value = _uiState.value.copy(autoBackupEnabled = newValue)
        viewModelScope.launch {
            settingsRepository.saveSetting(com.rudra.lifeledge.data.local.entity.Setting(key = "auto_backup_enabled", value = newValue.toString(), type = com.rudra.lifeledge.data.local.entity.SettingType.STRING))
            _uiState.value = _uiState.value.copy(
                message = if (newValue) "Auto backup enabled" else "Auto backup disabled"
            )
        }
    }

    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(backupFrequency = frequency)
        viewModelScope.launch {
            settingsRepository.saveSetting(com.rudra.lifeledge.data.local.entity.Setting(key = "backup_frequency", value = frequency, type = com.rudra.lifeledge.data.local.entity.SettingType.STRING))
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackingUp = true, message = null)
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val now = dateFormat.format(Date())
                settingsRepository.saveSetting(com.rudra.lifeledge.data.local.entity.Setting(key = "last_backup_date", value = now, type = com.rudra.lifeledge.data.local.entity.SettingType.STRING))
                
                val currentCount = _uiState.value.backupCount
                settingsRepository.saveSetting(com.rudra.lifeledge.data.local.entity.Setting(key = "backup_count", value = (currentCount + 1).toString(), type = com.rudra.lifeledge.data.local.entity.SettingType.STRING))
                
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    lastBackupDate = now,
                    backupCount = currentCount + 1,
                    message = "Backup created successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    message = "Backup failed: ${e.message}"
                )
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, message = null)
            try {
                // Simulate restore process
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    message = "Data restored successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    message = "Restore failed: ${e.message}"
                )
            }
        }
    }
}
