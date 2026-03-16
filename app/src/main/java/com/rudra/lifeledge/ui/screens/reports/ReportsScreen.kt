package com.rudra.lifeledge.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
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
                LifeScoreCard()
            }

            item {
                ReportTypeSelector(
                    selectedType = uiState.selectedReportType,
                    onTypeSelected = { viewModel.selectReportType(it) }
                )
            }

            item {
                FinancialSummaryReport()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WorkSummaryReport(modifier = Modifier.weight(1f))
                    HabitSummaryReport(modifier = Modifier.weight(1f))
                }
            }

            item {
                GoalsProgressReport()
            }

            item {
                ExportCard()
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LifeScoreCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Life Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "82",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "/ 100",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreBreakdownItem(label = "Financial", score = 78, icon = Icons.Default.AccountBalance)
                ScoreBreakdownItem(label = "Work", score = 85, icon = Icons.Default.Work)
                ScoreBreakdownItem(label = "Habits", score = 80, icon = Icons.Default.CheckCircle)
                ScoreBreakdownItem(label = "Goals", score = 85, icon = Icons.Default.Flag)
            }
        }
    }
}

@Composable
fun ScoreBreakdownItem(label: String, score: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ReportTypeSelector(
    selectedType: ReportType,
    onTypeSelected: (ReportType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.title) },
                leadingIcon = {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun FinancialSummaryReport() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Success.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Success)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportMetricCard(
                    label = "Income",
                    value = "৳52,000",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ReportMetricCard(
                    label = "Expense",
                    value = "৳31,500",
                    color = Error,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportMetricCard(
                    label = "Net Savings",
                    value = "৳20,500",
                    color = Secondary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ReportMetricCard(
                    label = "Savings Rate",
                    value = "39.4%",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ReportMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun WorkSummaryReport(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Work, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Work",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ReportRow(label = "Hours", value = "38h", color = Primary)
            ReportRow(label = "Overtime", value = "2h", color = Accent)
            ReportRow(label = "Days", value = "5", color = Secondary)
            ReportRow(label = "Balance", value = "95/100", color = Success)
        }
    }
}

@Composable
fun HabitSummaryReport(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Secondary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Habits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ReportRow(label = "Today", value = "5/6", color = Secondary)
            ReportRow(label = "Weekly", value = "82%", color = Primary)
            ReportRow(label = "Streak", value = "12 days", color = Accent)
            ReportRow(label = "Active", value = "4", color = Success)
        }
    }
}

@Composable
fun GoalsProgressReport() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Warning.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = Warning)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Goals Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            GoalProgressRow("Emergency Fund", 7500f, 10000f, Warning)
            GoalProgressRow("Vacation", 3500f, 5000f, Primary)
            GoalProgressRow("New Laptop", 4500f, 8000f, Secondary)
        }
    }
}

@Composable
fun GoalProgressRow(name: String, current: Float, target: Float, color: Color) {
    val progress = (current / target).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text("৳${current.toInt()}/৳${target.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ExportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Export Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF")
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JSON")
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV")
                }
            }
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
