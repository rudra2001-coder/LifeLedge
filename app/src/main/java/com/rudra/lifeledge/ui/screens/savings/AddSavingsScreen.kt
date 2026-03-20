package com.rudra.lifeledge.ui.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.SavingGoal
import com.rudra.lifeledge.data.repository.SavingRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

data class AddSavingsUiState(
    val amount: String = "",
    val selectedGoalId: Long? = null,
    val note: String = "",
    val selectedSource: String = "CASH",
    val goals: List<SavingGoal> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AddSavingsViewModel(
    private val savingRepository: SavingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddSavingsUiState())
    val uiState: StateFlow<AddSavingsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            savingRepository.getActiveGoals().collect { goals ->
                _uiState.value = _uiState.value.copy(goals = goals)
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateGoal(goalId: Long?) {
        _uiState.value = _uiState.value.copy(selectedGoalId = goalId)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateSource(source: String) {
        _uiState.value = _uiState.value.copy(selectedSource = source)
    }

    fun addSavings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val amountValue = _uiState.value.amount.toDoubleOrNull() ?: 0.0
                if (amountValue > 0) {
                    savingRepository.addSaving(
                        amount = amountValue,
                        goalId = _uiState.value.selectedGoalId,
                        note = _uiState.value.note.ifBlank { null },
                        source = _uiState.value.selectedSource
                    )
                    _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Please enter a valid amount", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: AddSavingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Savings", fontWeight = FontWeight.Bold) },
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
                SavingsAmountCard(
                    amount = uiState.amount,
                    onAmountChange = { viewModel.updateAmount(it) }
                )
            }

            item {
                SavingsSourceCard(
                    selectedSource = uiState.selectedSource,
                    onSourceSelected = { viewModel.updateSource(it) }
                )
            }

            item {
                SavingsGoalCard(
                    goals = uiState.goals,
                    selectedGoalId = uiState.selectedGoalId,
                    onGoalSelected = { viewModel.updateGoal(it) }
                )
            }

            item {
                SavingsNoteCard(
                    note = uiState.note,
                    onNoteChange = { viewModel.updateNote(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.addSavings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.amount.isNotBlank()
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error and clear
        }
    }
}

@Composable
fun SavingsAmountCard(amount: String, onAmountChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Savings Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = Secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "৳",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onAmountChange(newValue)
                        }
                    },
                    modifier = Modifier.width(200.dp),
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun SavingsSourceCard(
    selectedSource: String,
    onSourceSelected: (String) -> Unit
) {
    val sources = listOf(
        "CASH" to "Cash",
        "CARD" to "Card",
        "INCOME" to "Income",
        "WALLET" to "Wallet"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Source",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sources) { (value, label) ->
                    SourceChip(
                        label = label,
                        isSelected = selectedSource == value,
                        onClick = { onSourceSelected(value) }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) Secondary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (label) {
                    "Cash" -> Icons.Default.AccountBalanceWallet
                    "Card" -> Icons.Default.CreditCard
                    "Income" -> Icons.Default.TrendingUp
                    "Wallet" -> Icons.Default.Wallet
                    else -> Icons.Default.Savings
                },
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SavingsGoalCard(
    goals: List<SavingGoal>,
    selectedGoalId: Long?,
    onGoalSelected: (Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Save to Goal (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // General Savings option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onGoalSelected(null) }
                    .background(
                        if (selectedGoalId == null) Secondary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedGoalId == null,
                    onClick = { onGoalSelected(null) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("General Savings", fontWeight = FontWeight.Medium)
                    Text(
                        "Save without specific goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Goal options
            goals.forEach { goal ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGoalSelected(goal.id) }
                        .background(
                            if (selectedGoalId == goal.id) Color(goal.color).copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGoalId == goal.id,
                        onClick = { onGoalSelected(goal.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(goal.title, fontWeight = FontWeight.Medium)
                        Text(
                            "৳${formatNumber(goal.savedAmount)} / ৳${formatNumber(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(goal.color),
                        trackColor = Color(goal.color).copy(alpha = 0.2f)
                    )
                }
            }

            if (goals.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No active goals. Create a goal first!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SavingsNoteCard(note: String, onNoteChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Note (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
        }
    }
}