package com.rudra.lifeledge.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.ui.navigation.Screen
import com.rudra.lifeledge.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExpandableFab(
                expanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                onIncomeClick = {
                    isFabExpanded = false
                    navController.navigate(Screen.Income.route)
                },
                onExpenseClick = {
                    isFabExpanded = false
                    navController.navigate(Screen.Expense.route)
                },
                onSavingsClick = {
                    isFabExpanded = false
                    navController.navigate(Screen.AddSavings.route)
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            DashboardShimmerLoader()
        } else {
            DashboardContent(
                uiState = uiState,
                paddingValues = paddingValues,
                navController = navController,
                onRefresh = { viewModel.loadData() }
            )
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    paddingValues: PaddingValues,
    navController: NavController,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {


            item {
                FinancialSummarySection(
                    uiState = uiState,
                    onViewAllFinance = { navController.navigate(Screen.Finance.route) }
                )
            }

            item {
                WorkLifeSection(
                    weeklyHours = uiState.weeklyWorkHours,
                    weeklyDays = uiState.weeklyWorkDays,
                    workLifeBalance = uiState.workLifeBalance,
                    onLogWork = { navController.navigate(Screen.Work.route) }
                )
            }

            item {
                HabitsSection(
                    activeHabits = uiState.activeHabits,
                    completedToday = uiState.habitsCompletedToday,
                    longestStreak = uiState.longestStreak,
                    onViewHabits = { navController.navigate(Screen.Habits.route) }
                )
            }

            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    RecentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        onViewAll = { navController.navigate(Screen.Finance.route) }
                    )
                }
            }

            if (uiState.upcomingRecurring.isNotEmpty()) {
                item {
                    UpcomingRecurringSection(
                        recurring = uiState.upcomingRecurring,
                        onViewAll = { navController.navigate(Screen.RecurringTransactions.route) }
                    )
                }
            }

            if (uiState.activeGoals.isNotEmpty()) {
                item {
                    GoalsSection(
                        goals = uiState.activeGoals,
                        onViewAll = { navController.navigate(Screen.Goals.route) }
                    )
                }
            }

            item {
                QuickActionsGrid(
                    onAddExpense = { navController.navigate(Screen.Expense.route) },
                    onLogWork = { navController.navigate(Screen.Work.route) },
                    onAddHabit = { navController.navigate(Screen.Habits.route) },
                    onWriteJournal = { navController.navigate(Screen.Journal.route) },
                    onViewAnalytics = { navController.navigate(Screen.Reports.route) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    )
}

@Composable
fun FinancialSummarySection(
    uiState: DashboardUiState,
    onViewAllFinance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Financial Overview",
                icon = Icons.Outlined.AccountBalanceWallet,
                onViewAll = onViewAllFinance
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Net Balance Highlight
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.netBalance >= 0) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        "Net Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatCurrency(uiState.netBalance),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.netBalance >= 0) Success else Error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Summary
            DailySummaryRow(
                income = uiState.dailyIncome,
                expense = uiState.dailyExpense,
                netBalance = uiState.dailyNetBalance
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Summary
            MonthlySummaryRow(
                income = uiState.monthlyIncome,
                expense = uiState.monthlyExpense,
                savingsRate = uiState.savingsRate
            )


        }
    }
}

@Composable
fun DailySummaryRow(income: Double, expense: Double, netBalance: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DailySummaryItem(
            icon = Icons.Outlined.ArrowDownward,
            label = "Income",
            value = formatCurrency(income),
            color = Success,
            backgroundColor = Success.copy(alpha = 0.1f)
        )
        DailySummaryItem(
            icon = Icons.Outlined.ArrowUpward,
            label = "Expense",
            value = formatCurrency(expense),
            color = Error,
            backgroundColor = Error.copy(alpha = 0.1f)
        )
        DailySummaryItem(
            icon = if (netBalance >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
            label = "Net",
            value = formatCurrency(netBalance),
            color = if (netBalance >= 0) Success else Error,
            backgroundColor = if (netBalance >= 0) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun DailySummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    backgroundColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MonthlySummaryRow(income: Double, expense: Double, savingsRate: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonthlySummaryItem(
            label = "Monthly Income",
            value = formatCurrency(income),
            color = Success
        )
        MonthlySummaryItem(
            label = "Monthly Expense",
            value = formatCurrency(expense),
            color = Error
        )
        MonthlySummaryItem(
            label = "Savings Rate",
            value = String.format("%.1f%%", savingsRate),
            color = if (savingsRate >= 20) Success else if (savingsRate >= 10) Warning else Error
        )
    }
}

@Composable
fun MonthlySummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Composable
fun WorkLifeSection(
    weeklyHours: Double,
    weeklyDays: List<WorkDayUi>,
    workLifeBalance: Int,
    onLogWork: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Work-Life Balance",
                icon = Icons.Outlined.Work,
                actionIcon = Icons.Outlined.Add,
                onAction = onLogWork
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Balance Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$workLifeBalance/100",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                // Weekly Hours Progress
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Weekly Hours",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${String.format("%.1f", weeklyHours)}h / 40h",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Calendar
            WeeklyCalendar(weeklyDays)
        }
    }
}

@Composable
fun WeeklyCalendar(weeklyDays: List<WorkDayUi>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weeklyDays.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (day.isWorkingDay)
                        Success.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            day.dayName.take(1),
                            fontWeight = if (day.isWorkingDay) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isWorkingDay) Success else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (day.isWorkingDay) "${day.hoursWorked.toInt()}h" else "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isWorkingDay) Success else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HabitsSection(
    activeHabits: Int,
    completedToday: Int,
    longestStreak: Int,
    onViewHabits: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Habits",
                icon = Icons.Outlined.CheckCircle,
                actionIcon = Icons.Outlined.Add,
                onAction = onViewHabits
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HabitStatItem(
                    value = "$completedToday/$activeHabits",
                    label = "Today",
                    icon = Icons.Outlined.Today,
                    color = Secondary
                )
                HabitStatItem(
                    value = "$longestStreak days",
                    label = "Best Streak",
                    icon = Icons.Outlined.Whatshot,
                    color = Accent
                )
            }

            if (activeHabits > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (completedToday.toFloat() / activeHabits).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Secondary,
                    trackColor = Secondary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun HabitStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<TransactionUi>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Recent Transactions",
                icon = Icons.Outlined.History,
                onViewAll = onViewAll
            )

            Spacer(modifier = Modifier.height(12.dp))

            transactions.take(5).forEach { transaction ->
                EnhancedTransactionRow(transaction)
                if (transaction != transactions.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedTransactionRow(transaction: TransactionUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (transaction.isExpense) Error.copy(alpha = 0.1f) else Success.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.isExpense) Icons.Outlined.ShoppingBag else Icons.Outlined.Payment,
                        contentDescription = null,
                        tint = if (transaction.isExpense) Error else Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    transaction.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    transaction.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "${if (transaction.isExpense) "-" else "+"}${formatCurrency(transaction.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isExpense) Error else Success
        )
    }
}

@Composable
fun UpcomingRecurringSection(
    recurring: List<RecurringTransaction>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Upcoming Recurring",
                icon = Icons.Outlined.Repeat,
                onViewAll = onViewAll
            )

            Spacer(modifier = Modifier.height(12.dp))

            recurring.forEach { item ->
                UpcomingRecurringRow(item)
                if (item != recurring.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Primary.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingRecurringRow(item: RecurringTransaction) {
    val isIncome = item.type == TransactionType.INCOME
    val color = if (isIncome) Success else Error

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isIncome) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Next: ${item.nextDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "${if (isIncome) "+" else "-"}${formatCurrency(item.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun GoalsSection(
    goals: List<GoalUi>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SectionHeader(
                title = "Goals Progress",
                icon = Icons.Outlined.EmojiEvents,
                onViewAll = onViewAll
            )

            Spacer(modifier = Modifier.height(12.dp))

            goals.take(3).forEach { goal ->
                EnhancedGoalRow(goal)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun EnhancedGoalRow(goal: GoalUi) {
    val progress = (goal.current / goal.target).coerceIn(0.0, 1.0).toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                goal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${formatNumber(goal.current)}/${formatNumber(goal.target)} ${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(goal.color),
            trackColor = Color(goal.color).copy(alpha = 0.2f)
        )

        Text(
            "${(progress * 100).toInt()}% Complete",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun QuickActionsGrid(
    onAddExpense: () -> Unit,
    onLogWork: () -> Unit,
    onAddHabit: () -> Unit,
    onWriteJournal: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedQuickActionButton(
                    icon = Icons.Outlined.AddShoppingCart,
                    label = "Expense",
                    color = Error,
                    onClick = onAddExpense
                )
                EnhancedQuickActionButton(
                    icon = Icons.Outlined.Work,
                    label = "Work",
                    color = Primary,
                    onClick = onLogWork
                )
                EnhancedQuickActionButton(
                    icon = Icons.Outlined.FitnessCenter,
                    label = "Habit",
                    color = Secondary,
                    onClick = onAddHabit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedQuickActionButton(
                    icon = Icons.Outlined.Edit,
                    label = "Journal",
                    color = Accent,
                    onClick = onWriteJournal
                )
                EnhancedQuickActionButton(
                    icon = Icons.Outlined.BarChart,
                    label = "Analytics",
                    color = Color(0xFF9C27B0),
                    onClick = onViewAnalytics
                )
                // Empty placeholder for symmetry
                Box(modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Composable
fun EnhancedQuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier
                .size(60.dp)
                .shadow(4.dp, CircleShape)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    actionIcon: ImageVector? = null,
    onViewAll: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text("View All")
            }
        } else if (onAction != null && actionIcon != null) {
            IconButton(onClick = onAction) {
                Icon(
                    actionIcon,
                    contentDescription = "Add",
                    tint = Primary
                )
            }
        }
    }
}

@Composable
fun ExpandableFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onSavingsClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedMiniFab(
                    icon = Icons.Outlined.Savings,
                    label = "Savings",
                    color = Secondary,
                    onClick = {
                        onExpandedChange(false)
                        onSavingsClick()
                    }
                )
                EnhancedMiniFab(
                    icon = Icons.Outlined.RemoveShoppingCart,
                    label = "Expense",
                    color = Error,
                    onClick = {
                        onExpandedChange(false)
                        onExpenseClick()
                    }
                )
                EnhancedMiniFab(
                    icon = Icons.Outlined.AddCard,
                    label = "Income",
                    color = Success,
                    onClick = {
                        onExpandedChange(false)
                        onIncomeClick()
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = Primary,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.Close else Icons.Outlined.Add,
                contentDescription = if (expanded) "Close" else "Add",
                tint = Color.White
            )
        }
    }
}

@Composable
fun EnhancedMiniFab(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = color
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun DashboardShimmerLoader() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(8) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {}
        }
    }
}

// Helper functions
fun formatCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("৳%.1fM", amount / 1_000_000)
        amount >= 100_000 -> String.format("৳%.1fL", amount / 100_000)
        amount >= 1_000 -> String.format("৳%.1fK", amount / 1_000)
        else -> String.format("৳%.0f", amount)
    }
}

fun formatNumber(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format("%.1fK", value / 1_000)
        value == value.toInt().toDouble() -> value.toInt().toString()
        else -> String.format("%.1f", value)
    }
}