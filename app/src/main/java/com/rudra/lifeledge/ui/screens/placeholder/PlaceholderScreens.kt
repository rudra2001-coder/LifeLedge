package com.rudra.lifeledge.ui.screens.placeholder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEntryScreen(onNavigateBack: () -> Unit) {
    PlaceholderScreen(title = "Add Entry")
}

@Composable
fun AllFunsionScreen(navController: androidx.navigation.NavHostController) {
    PlaceholderScreen(title = "All Features")
}

@Composable
fun MindfulBreakScreen() {
    PlaceholderScreen(title = "Mindful Break")
}

@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    PlaceholderScreen(title = "Calculation")
}

@Composable
fun CreditCardScreen() {
    PlaceholderScreen(title = "Credit Card")
}

@Composable
fun EmiScreen() {
    PlaceholderScreen(title = "EMI")
}

@Composable
fun FinancialStatementScreen() {
    PlaceholderScreen(title = "Financial Statement")
}

@Composable
fun LoansScreen() {
    PlaceholderScreen(title = "Loans")
}

@Composable
fun MonthlyReportScreen(onNavigateBack: () -> Unit) {
    PlaceholderScreen(title = "Monthly Report")
}

@Composable
fun OvertimeScreen() {
    PlaceholderScreen(title = "Overtime")
}

@Composable
fun SchedulerScreen() {
    PlaceholderScreen(title = "Scheduler")
}

@Composable
fun TeamScreen(onNavigateBack: () -> Unit) {
    PlaceholderScreen(title = "Team")
}

@Composable
fun WisdomScreen() {
    PlaceholderScreen(title = "Wisdom")
}

@Composable
fun WorkTimerScreen() {
    PlaceholderScreen(title = "Work Timer")
}

@Composable
fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This feature is coming soon!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
