package com.rudra.lifeledge.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.rudra.lifeledge.data.local.entity.Goal
import com.rudra.lifeledge.data.local.entity.GoalType
import com.rudra.lifeledge.data.repository.GoalRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddGoalUiState(
    val title: String = "",
    val description: String = "",
    val selectedType: GoalType = GoalType.FINANCIAL,
    val targetValue: String = "",
    val unit: String = "",
    val targetDate: String = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val selectedIcon: String = "🎯",
    val selectedColor: Long = 0xFF3B82F6,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AddGoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Goal", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            GoalTypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.updateType(it) }
            )

            GoalTitleInput(
                title = uiState.title,
                onTitleChange = { viewModel.updateTitle(it) }
            )

            GoalDescriptionInput(
                description = uiState.description,
                onDescriptionChange = { viewModel.updateDescription(it) }
            )

            GoalTargetInput(
                targetValue = uiState.targetValue,
                unit = uiState.unit,
                onTargetChange = { viewModel.updateTargetValue(it) },
                onUnitChange = { viewModel.updateUnit(it) }
            )

            GoalDateInput(
                targetDate = uiState.targetDate,
                onDateChange = { viewModel.updateTargetDate(it) }
            )

            IconColorSelector(
                selectedIcon = uiState.selectedIcon,
                selectedColor = uiState.selectedColor,
                onIconSelected = { viewModel.updateIcon(it) },
                onColorSelected = { viewModel.updateColor(it) }
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = Error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveGoal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState.title.isNotBlank() && uiState.targetValue.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GoalTypeSelector(
    selectedType: GoalType,
    onTypeSelected: (GoalType) -> Unit
) {
    Column {
        Text(
            "Goal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalType.entries.forEach { type ->
                val isSelected = selectedType == type
                val (icon, label, color) = when (type) {
                    GoalType.FINANCIAL -> Triple(Icons.Default.AccountBalance, "Finance", 0xFF22C55E)
                    GoalType.WORK -> Triple(Icons.Default.Work, "Work", 0xFF3B82F6)
                    GoalType.HEALTH -> Triple(Icons.Default.Favorite, "Health", 0xFFEF4444)
                    GoalType.LEARNING -> Triple(Icons.Default.School, "Learn", 0xFFF59E0B)
                    GoalType.CUSTOM -> Triple(Icons.Default.Star, "Custom", 0xFF8B5CF6)
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelected(type) },
                    label = { Text(label) },
                    leadingIcon = {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(color).copy(alpha = 0.2f),
                        selectedLabelColor = Color(color)
                    )
                )
            }
        }
    }
}

@Composable
fun GoalTitleInput(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column {
        Text(
            "Goal Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Save for Laptop") },
            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun GoalDescriptionInput(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column {
        Text(
            "Description (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Describe your goal...") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
fun GoalTargetInput(
    targetValue: String,
    unit: String,
    onTargetChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    Column {
        Text(
            "Target",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = targetValue,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) onTargetChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("10000") },
                label = { Text("Target Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tk") },
                label = { Text("Unit") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
fun GoalDateInput(
    targetDate: String,
    onDateChange: (String) -> Unit
) {
    Column {
        Text(
            "Target Date",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = targetDate,
            onValueChange = onDateChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("2024-12-31") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun IconColorSelector(
    selectedIcon: String,
    selectedColor: Long,
    onIconSelected: (String) -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val icons = listOf("🎯", "💰", "💼", "🏃", "📚", "🏠", "🚗", "✈️", "📱", "💍", "🎓", "🎉")
    val colors = listOf(0xFF3B82F6, 0xFF22C55E, 0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899, 0xFF06B6D4, 0xFF6366F1)

    Column {
        Text(
            "Icon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(icons) { icon ->
                val isSelected = selectedIcon == icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onIconSelected(icon) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colors) { color ->
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

class AddGoalViewModel(private val goalRepository: GoalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddGoalUiState())
    val uiState: StateFlow<AddGoalUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateType(type: GoalType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun updateTargetValue(value: String) {
        _uiState.value = _uiState.value.copy(targetValue = value, errorMessage = null)
    }

    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun updateTargetDate(date: String) {
        _uiState.value = _uiState.value.copy(targetDate = date)
    }

    fun updateIcon(icon: String) {
        _uiState.value = _uiState.value.copy(selectedIcon = icon)
    }

    fun updateColor(color: Long) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun saveGoal() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a goal title")
            return
        }

        val targetValue = state.targetValue.toDoubleOrNull()
        if (targetValue == null || targetValue <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid target value")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val goal = Goal(
                    title = state.title,
                    description = state.description.ifBlank { null },
                    type = state.selectedType,
                    targetValue = targetValue,
                    currentValue = 0.0,
                    unit = state.unit.ifBlank { "Tk" },
                    startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    targetDate = state.targetDate,
                    isCompleted = false,
                    completedDate = null,
                    icon = state.selectedIcon,
                    color = state.selectedColor.toInt(),
                    milestones = ""
                )
                goalRepository.saveGoal(goal)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to create goal. Please try again."
                )
            }
        }
    }
}
