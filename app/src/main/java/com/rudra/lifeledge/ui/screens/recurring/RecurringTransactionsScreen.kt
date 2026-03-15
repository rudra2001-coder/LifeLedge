package com.rudra.lifeledge.ui.screens.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.Frequency
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun RecurringTransactionsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    viewModel: RecurringTransactionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecurringTransactions()
    }

    if (uiState.editingRecurring != null) {
        EditRecurringTransactionScreen(
            recurring = uiState.editingRecurring!!,
            navController = navController,
            onSave = { viewModel.updateRecurring(it) },
            onNavigateBack = { viewModel.setEditingRecurring(null) }
        )
    } else {
        RecurringTransactionsContent(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onToggleActive = { viewModel.toggleActive(it) },
            onDelete = { viewModel.deleteRecurring(it) },
            onEdit = { viewModel.setEditingRecurring(it) }
        )
    }
}

@Composable
private fun RecurringTransactionsContent(
    uiState: RecurringTransactionsUiState,
    onNavigateBack: () -> Unit,
    onToggleActive: (RecurringTransaction) -> Unit,
    onDelete: (RecurringTransaction) -> Unit,
    onEdit: (RecurringTransaction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Transactions", fontWeight = FontWeight.Bold) },
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
        if (uiState.recurringTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "No recurring transactions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Add income or expense with recurring enabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    RecurringStatsCard(
                        totalIncome = uiState.totalMonthlyIncome,
                        totalExpense = uiState.totalMonthlyExpense
                    )
                }

                item {
                    Text(
                        "Upcoming",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.recurringTransactions) { recurring ->
                    RecurringTransactionCard(
                        recurring = recurring,
                        onToggleActive = { onToggleActive(recurring) },
                        onDelete = { onDelete(recurring) },
                        onEdit = { onEdit(recurring) }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun RecurringStatsCard(
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Monthly Income",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
                Text(
                    "৳${String.format("%.0f", totalIncome)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Success
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Monthly Expense",
                    style = MaterialTheme.typography.bodySmall,
                    color = Error
                )
                Text(
                    "৳${String.format("%.0f", totalExpense)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Error
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary
                )
                Text(
                    "৳${String.format("%.0f", totalIncome - totalExpense)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun RecurringTransactionCard(
    recurring: RecurringTransaction,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val nextDate = LocalDate.parse(recurring.nextDate)
    val daysUntil = ChronoUnit.DAYS.between(today, nextDate).toInt()

    val isIncome = recurring.type == TransactionType.INCOME
    val color = if (isIncome) Success else Error

    val nextDateFormatted = nextDate.format(DateTimeFormatter.ofPattern("MMM d"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recurring.isActive) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = color
                        )
                    }
                    Column {
                        Text(
                            recurring.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${getFrequencyText(recurring.frequency)} - ${getDayText(recurring)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "৳${String.format("%.0f", recurring.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Next: $nextDateFormatted (${
                        when {
                            daysUntil < 0 -> "Overdue"
                            daysUntil == 0 -> "Today"
                            daysUntil == 1 -> "Tomorrow"
                            else -> "In $daysUntil days"
                        }
                    })",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysUntil <= 3) Warning else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Primary
                        )
                    }
                    IconButton(
                        onClick = onToggleActive,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (recurring.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (recurring.isActive) "Pause" else "Resume",
                            tint = if (recurring.isActive) Warning else Success
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recurring Transaction") },
            text = { Text("Are you sure you want to delete \"${recurring.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getFrequencyText(frequency: Frequency): String {
    return when (frequency) {
        Frequency.DAILY -> "Daily"
        Frequency.WEEKLY -> "Weekly"
        Frequency.MONTHLY -> "Monthly"
        Frequency.YEARLY -> "Yearly"
    }
}

private fun getDayText(recurring: RecurringTransaction): String {
    return when (recurring.frequency) {
        Frequency.DAILY -> "Every day"
        Frequency.WEEKLY -> "Every week"
        Frequency.MONTHLY -> "${recurring.executeDay?.let { "${getOrdinal(it)} of month" } ?: "Monthly"}"
        Frequency.YEARLY -> "Yearly"
    }
}

private fun getOrdinal(day: Int): String {
    return when (day) {
        1, 21, 31 -> "${day}st"
        2, 22 -> "${day}nd"
        3, 23 -> "${day}rd"
        else -> "${day}th"
    }
}
