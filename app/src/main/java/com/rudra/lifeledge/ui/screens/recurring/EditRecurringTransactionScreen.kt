package com.rudra.lifeledge.ui.screens.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.Frequency
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.ui.screens.income.RecurringFrequency
import com.rudra.lifeledge.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringTransactionScreen(
    recurring: RecurringTransaction,
    navController: NavController,
    onSave: (RecurringTransaction) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(recurring.name) }
    var amount by remember { mutableStateOf(recurring.amount.toString()) }
    var isActive by remember { mutableStateOf(recurring.isActive) }
    var selectedFrequency by remember { 
        mutableStateOf(
            when (recurring.frequency) {
                Frequency.DAILY -> RecurringFrequency.DAILY
                Frequency.WEEKLY -> RecurringFrequency.WEEKLY
                Frequency.MONTHLY -> RecurringFrequency.MONTHLY
                Frequency.YEARLY -> RecurringFrequency.YEARLY
            }
        ) 
    }
    var executeDay by remember { mutableStateOf(recurring.executeDay ?: 1) }
    var notes by remember { mutableStateOf(recurring.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recurring", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (recurring.type == TransactionType.INCOME) 
                            Success.copy(alpha = 0.1f) 
                        else 
                            Error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (recurring.type == TransactionType.INCOME) 
                                    Icons.Default.TrendingUp 
                                else 
                                    Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (recurring.type == TransactionType.INCOME) Success else Error
                            )
                            Text(
                                if (recurring.type == TransactionType.INCOME) "Income" else "Expense",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (recurring.type == TransactionType.INCOME) Success else Error
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("৳") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Frequency",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RecurringFrequency.entries.forEach { freq ->
                                FilterChip(
                                    selected = selectedFrequency == freq,
                                    onClick = { selectedFrequency = freq },
                                    label = { Text(freq.title) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        if (selectedFrequency == RecurringFrequency.MONTHLY) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Day of month: $executeDay",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = executeDay.toFloat(),
                                onValueChange = { executeDay = it.toInt() },
                                valueRange = 1f..28f,
                                steps = 26,
                                colors = SliderDefaults.colors(
                                    thumbColor = Primary, 
                                    activeTrackColor = Primary
                                )
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Active",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isActive) "Transactions will be added" else "Paused - no auto transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: recurring.amount
                        val frequency = when (selectedFrequency) {
                            RecurringFrequency.DAILY -> Frequency.DAILY
                            RecurringFrequency.WEEKLY -> Frequency.WEEKLY
                            RecurringFrequency.MONTHLY -> Frequency.MONTHLY
                            RecurringFrequency.YEARLY -> Frequency.YEARLY
                        }
                        
                        val updated = recurring.copy(
                            name = name,
                            amount = amountValue,
                            frequency = frequency,
                            executeDay = if (frequency == Frequency.MONTHLY) executeDay else null,
                            notes = notes.ifBlank { null },
                            isActive = isActive
                        )
                        onSave(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
