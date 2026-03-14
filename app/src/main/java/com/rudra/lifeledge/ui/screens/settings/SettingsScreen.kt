package com.rudra.lifeledge.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var autoBackupEnabled by remember { mutableStateOf(true) }
    var currency by remember { mutableStateOf("BDT (৳)") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsSection(title = "Appearance")
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            item {
                SettingsSection(title = "Security")
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Lock",
                    subtitle = "Use fingerprint or face to unlock",
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = "Set PIN or pattern",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Incognito Mode",
                    subtitle = "Hide amounts in notifications",
                    onClick = { }
                )
            }

            item {
                SettingsSection(title = "Data & Backup")
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Backup,
                    title = "Auto Backup",
                    subtitle = "Backup data daily",
                    checked = autoBackupEnabled,
                    onCheckedChange = { autoBackupEnabled = it }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = "Restore Backup",
                    subtitle = "Restore from a backup file",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Export Data",
                    subtitle = "Export all data as JSON or CSV",
                    onClick = { }
                )
            }

            item {
                SettingsSection(title = "Preferences")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency",
                    subtitle = currency,
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = { }
                )
            }

            item {
                SettingsSection(title = "About")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About LifeLedger",
                    subtitle = "Version 1.0.0",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help with the app",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Your data stays on your device",
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = Primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
