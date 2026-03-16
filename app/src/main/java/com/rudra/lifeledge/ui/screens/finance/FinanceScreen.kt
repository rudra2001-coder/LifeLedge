package com.rudra.lifeledge.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.ui.navigation.Screen
import com.rudra.lifeledge.ui.screens.savings.EmptyStateCard
import com.rudra.lifeledge.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    navController: NavController,
    viewModel: FinanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.recentlyDeletedTransaction) {
        uiState.recentlyDeletedTransaction?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDeleteTransaction()
            } else {
                viewModel.clearDeletedTransaction()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Center", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Action Buttons
            QuickActionsRow(
                onIncomeClick = { navController.navigate(Screen.Income.route) },
                onExpenseClick = { navController.navigate(Screen.Expense.route) },
                onSavingsClick = { navController.navigate(Screen.Savings.route) }
            )

            BalanceSummaryCard(
                totalBalance = uiState.totalBalance,
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpense = uiState.monthlyExpense
            )

            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Accounts") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Transactions") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Budget") }
                )
            }

            when (uiState.selectedTab) {
                0 -> AccountsTab(accounts = uiState.accounts)
                1 -> TransactionsTab(transactions = uiState.transactions, onDelete = { viewModel.deleteTransaction(it) })
                2 -> BudgetTab()
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onSavingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionChip(
            icon = Icons.Default.Add,
            label = "Income",
            color = Success,
            onClick = onIncomeClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionChip(
            icon = Icons.Default.Remove,
            label = "Expense",
            color = Error,
            onClick = onExpenseClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionChip(
            icon = Icons.Default.Savings,
            label = "Savings",
            color = Secondary,
            onClick = onSavingsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BalanceSummaryCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                formatCurrency(totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "+${formatCurrency(monthlyIncome)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        "Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "-${formatCurrency(monthlyExpense)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AccountsTab(accounts: List<Account>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (accounts.isEmpty()) {
            item {
                EmptyStateCard(message = "No accounts yet. Add your first account!")
            }
        } else {
            items(accounts) { account ->
                AccountCard(account = account)
            }
        }
    }
}

@Composable
fun AccountCard(account: Account) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (account.type.name) {
                        "CASH" -> Icons.Default.AccountBalanceWallet
                        "BANK" -> Icons.Default.AccountBalance
                        "MOBILE_BANKING" -> Icons.Default.PhoneAndroid
                        "CREDIT_CARD" -> Icons.Default.CreditCard
                        "SAVINGS" -> Icons.Default.Savings
                        else -> Icons.Default.AccountBalance
                    },
                    contentDescription = null,
                    tint = Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    account.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatCurrency(account.balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (account.balance >= 0) Success else Error
            )
        }
    }
}

@Composable
fun TransactionsTab(transactions: List<Transaction>, onDelete: (Transaction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(message = "No transactions yet")
            }
        } else {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction, onDelete = { onDelete(transaction) })
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (transaction.type == TransactionType.EXPENSE) Error.copy(alpha = 0.1f)
                        else Success.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        TransactionType.EXPENSE -> Icons.Default.ArrowDownward
                        TransactionType.INCOME -> Icons.Default.ArrowUpward
                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                        TransactionType.SAVE -> Icons.Default.Savings
                        TransactionType.TRANSFER_FROM_SAVING -> Icons.Default.AccountBalance
                    },
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.EXPENSE) Error else Success
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        transaction.payee ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Recurring",
                            modifier = Modifier.size(14.dp),
                            tint = Primary
                        )
                    }
                }
                Text(
                    transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.EXPENSE) Error else Success
                )
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = Error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
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

@Composable
fun BudgetTab() {
    val budgetCategories = listOf(
        BudgetCategoryItem("Food & Dining", 5000f, 3200f, Color(0xFFEF4444)),
        BudgetCategoryItem("Transport", 3000f, 1800f, Color(0xFF3B82F6)),
        BudgetCategoryItem("Shopping", 4000f, 2500f, Color(0xFFEC4899)),
        BudgetCategoryItem("Entertainment", 2000f, 800f, Color(0xFF8B5CF6)),
        BudgetCategoryItem("Bills & Utilities", 3500f, 3500f, Color(0xFFF59E0B)),
        BudgetCategoryItem("Health", 2000f, 500f, Color(0xFF22C55E)),
        BudgetCategoryItem("Education", 3000f, 1500f, Color(0xFF06B6D4)),
        BudgetCategoryItem("Personal Care", 1500f, 700f, Color(0xFFF97316))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total Budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "৳24,000",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "৳14,500",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Success,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "60% of budget used",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        item {
            Text(
                "Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(budgetCategories) { category ->
            BudgetCategoryCard(category = category)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

data class BudgetCategoryItem(
    val name: String,
    val budget: Float,
    val spent: Float,
    val color: Color
)

@Composable
fun BudgetCategoryCard(category: BudgetCategoryItem) {
    val progress = (category.spent / category.budget).coerceIn(0f, 1f)
    val isOverBudget = category.spent > category.budget

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) Error.copy(alpha = 0.1f) 
                           else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(category.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "৳${category.spent.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) Error else category.color
                    )
                    Text(
                        "of ৳${category.budget.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) Error else category.color,
                trackColor = category.color.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${(progress * 100).toInt()}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (category.budget - category.spent > 0) {
                    Text(
                        "৳${(category.budget - category.spent).toInt()} left",
                        style = MaterialTheme.typography.bodySmall,
                        color = Success
                    )
                } else {
                    Text(
                        "৳${(category.spent - category.budget).toInt()} over",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "BD"))
    return format.format(amount)
}
