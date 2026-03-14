package com.rudra.lifeledge.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.theme.*

data class MoreMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String? = null
)

val moreMenuItems = listOf(
    MoreMenuItem("work", "Work Center", "Track hours & overtime", Icons.Default.Work, Color(0xFF3B82F6), "work"),
    MoreMenuItem("habits", "Habits", "Build daily routines", Icons.Default.CheckCircle, Color(0xFF22C55E), "habits"),
    MoreMenuItem("journal", "Journal", "Reflect & write", Icons.Default.Book, Color(0xFF8B5CF6), "journal"),
    MoreMenuItem("goals", "Goals", "Set & achieve goals", Icons.Default.Flag, Color(0xFFF59E0B), "goals"),
    MoreMenuItem("reports", "Reports", "Analytics & insights", Icons.Default.Analytics, Color(0xFF06B6D4), "reports"),
    MoreMenuItem("accounts", "Accounts", "Manage accounts", Icons.Default.AccountBalance, Color(0xFFEC4899)),
    MoreMenuItem("budgets", "Budgets", "Set spending limits", Icons.Default.PieChart, Color(0xFFF97316)),
    MoreMenuItem("recurring", "Recurring", "Auto transactions", Icons.Default.Repeat, Color(0xFF6366F1)),
    MoreMenuItem("loans", "Loans & EMI", "Track debts", Icons.Default.CreditCard, Color(0xFFEF4444)),
    MoreMenuItem("debts", "Debt Tracker", "Manage loans", Icons.Default.AccountBalanceWallet, Color(0xFFE11D48)),
    MoreMenuItem("calendar", "Calendar", "View all events", Icons.Default.CalendarMonth, Color(0xFF14B8A6)),
    MoreMenuItem("export", "Export Data", "JSON, CSV, PDF", Icons.Default.Download, Color(0xFF64748B))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickActionsSection(navController)
            }

            item {
                Text(
                    "All Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                FeaturesGrid(navController)
            }

            item {
                SettingsSection(navController)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Income",
                    onClick = { navController.navigate("income") }
                )
                QuickActionButton(
                    icon = Icons.Default.Remove,
                    label = "Expense",
                    onClick = { navController.navigate("expense") }
                )
                QuickActionButton(
                    icon = Icons.Default.Savings,
                    label = "Savings",
                    onClick = { navController.navigate("savings") }
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FeaturesGrid(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        moreMenuItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    FeatureCard(
                        item = item,
                        onClick = { 
                            item.route?.let { navController.navigate(it) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    item: MoreMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences",
                onClick = { navController.navigate("settings") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Manage your data",
                onClick = { }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = { }
            )
        }
    }
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
            .padding(16.dp),
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
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
