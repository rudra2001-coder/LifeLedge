package com.rudra.lifeledge.ui.screens.more

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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.navigation.Screen
import com.rudra.lifeledge.ui.theme.*

data class MoreMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String? = null
)

data class MoreSection(
    val title: String,
    val items: List<MoreMenuItem>
)

val financialSection = MoreSection(
    title = "Financial",
    items = listOf(
        MoreMenuItem("income", "Add Income", "Record your income", Icons.Default.TrendingUp, Success, Screen.Income.route),
        MoreMenuItem("expense", "Add Expense", "Track your spending", Icons.Default.TrendingDown, Error, Screen.Expense.route),
        MoreMenuItem("savings", "Add Savings", "Add to savings", Icons.Default.Savings, Secondary, Screen.AddSavings.route),
        MoreMenuItem("manage_savings", "Manage Savings", "Manage savings goals", Icons.Default.Savings, Color(0xFF8B5CF6), Screen.Savings.route),
        MoreMenuItem("transfer", "Transfer", "Move money", Icons.Default.SwapHoriz, Color(0xFF6366F1), Screen.Transfer.route),
        MoreMenuItem("cards", "Cards & Wallets", "Manage cards", Icons.Default.CreditCard, Color(0xFFEC4899), Screen.Cards.route),
        MoreMenuItem("accounts", "Accounts", "Manage bank accounts", Icons.Default.AccountBalance, Color(0xFFEC4899), Screen.Finance.route),
        MoreMenuItem("budgets", "Budgets", "Set spending limits", Icons.Default.PieChart, Color(0xFFF97316), Screen.Finance.route),
        MoreMenuItem("recurring", "Recurring Transactions", "Auto transactions", Icons.Default.Repeat, Color(0xFF8B5CF6), Screen.RecurringTransactions.route),
        MoreMenuItem("loans", "Loans & EMI", "Track debts", Icons.Default.CreditCard, Color(0xFFEF4444), Screen.Finance.route)
    )
)

val generalSection = MoreSection(
    title = "General",
    items = listOf(
        MoreMenuItem("work", "Work Center", "Track hours & overtime", Icons.Default.Work, Color(0xFF3B82F6), Screen.Work.route),
        MoreMenuItem("habits", "Habits", "Build daily routines", Icons.Default.CheckCircle, Color(0xFF22C55E), Screen.Habits.route),
        MoreMenuItem("journal", "Journal", "Reflect & write", Icons.Default.Book, Color(0xFF8B5CF6), Screen.Journal.route),
        MoreMenuItem("goals", "Goals", "Set & achieve goals", Icons.Default.Flag, Color(0xFFF59E0B), Screen.Goals.route),
        MoreMenuItem("reports", "Reports", "Analytics & insights", Icons.Default.Analytics, Color(0xFF06B6D4), Screen.Reports.route),
        MoreMenuItem("calendar", "Calendar", "View all events", Icons.Default.CalendarMonth, Color(0xFF14B8A6), Screen.Calendar.route)
    )
)

val settingsSection = MoreSection(
    title = "Settings",
    items = listOf(
        MoreMenuItem("settings", "Settings", "App preferences", Icons.Default.Settings, Color(0xFF64748B), Screen.Settings.route),
        MoreMenuItem("backup", "Backup & Restore", "Manage your data", Icons.Default.Backup, Color(0xFF64748B), Screen.Backup.route),
        MoreMenuItem("export", "Export Data", "JSON, CSV, PDF", Icons.Default.Download, Color(0xFF64748B), Screen.Export.route),
        MoreMenuItem("about", "About", "Version 1.0.0", Icons.Default.Info, Color(0xFF64748B), Screen.Settings.route)
    )
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                QuickActionsSection(navController)
            }

            item {
                SectionCard(
                    title = financialSection.title,
                    icon = Icons.Default.AccountBalance,
                    iconColor = Success,
                    items = financialSection.items,
                    navController = navController
                )
            }

            item {
                SectionCard(
                    title = generalSection.title,
                    icon = Icons.Default.GridView,
                    iconColor = Primary,
                    items = generalSection.items,
                    navController = navController
                )
            }

            item {
                SectionCard(
                    title = settingsSection.title,
                    icon = Icons.Default.Settings,
                    iconColor = Color(0xFF64748B),
                    items = settingsSection.items,
                    navController = navController
                )
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
                    icon = Icons.Default.TrendingUp,
                    label = "Income",
                    onClick = { navController.navigate(Screen.Income.route) }
                )
                QuickActionButton(
                    icon = Icons.Default.TrendingDown,
                    label = "Expense",
                    onClick = { navController.navigate(Screen.Expense.route) }
                )
                QuickActionButton(
                    icon = Icons.Default.Savings,
                    label = "Savings",
                    onClick = { navController.navigate(Screen.AddSavings.route) }
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    onClick = { navController.navigate(Screen.Transfer.route) }
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
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<MoreMenuItem>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        SectionItem(
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SectionItem(
    item: MoreMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(item.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
