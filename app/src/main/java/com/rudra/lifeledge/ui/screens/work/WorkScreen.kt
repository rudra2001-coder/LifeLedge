package com.rudra.lifeledge.ui.screens.work

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.DayType
import com.rudra.lifeledge.data.local.entity.WorkDay
import com.rudra.lifeledge.data.repository.WorkRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class WorkUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val workDays: List<WorkDay> = emptyList(),
    val selectedDate: LocalDate? = null,
    val weeklyHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(
    navController: NavController,
    viewModel: WorkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Center", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startOvertimeTimer() },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Timer, contentDescription = "Start Overtime", tint = Color.White)
            }
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
                WorkSummaryCard(
                    weeklyHours = uiState.weeklyHours,
                    overtimeHours = uiState.overtimeHours
                )
            }

            item {
                CalendarHeader(
                    currentMonth = uiState.currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            item {
                CalendarGrid(
                    currentMonth = uiState.currentMonth,
                    workDays = uiState.workDays,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            item {
                LegendCard()
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun WorkSummaryCard(weeklyHours: Double, overtimeHours: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${weeklyHours}h",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    "This Week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${overtimeHours}h",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Accent
                )
                Text(
                    "Overtime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(40.0 - weeklyHours).coerceAtLeast(0.0)}h",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
                Text(
                    "Remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        Text(
            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    workDays: List<WorkDay>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val workDaysMap = workDays.associateBy { it.date }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        var dayCounter = 1
        val rows = (daysInMonth + firstDayOfMonth + 6) / 7
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col - firstDayOfMonth + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayIndex)
                        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val workDay = workDaysMap[dateStr]
                        val isSelected = selectedDate == date
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> Primary
                                        isToday -> Primary.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$dayIndex",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (workDay != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                getDayTypeColor(workDay.dayType),
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF22C55E), label = "Work")
            LegendItem(color = Color(0xFFEAB308), label = "Home")
            LegendItem(color = Color(0xFF9CA3AF), label = "Off")
            LegendItem(color = Color(0xFF3B82F6), label = "Holiday")
            LegendItem(color = Color(0xFFEF4444), label = "Sick")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getDayTypeColor(dayType: DayType): Color {
    return when (dayType) {
        DayType.WORK -> Color(0xFF22C55E)
        DayType.HOME_OFFICE -> Color(0xFFEAB308)
        DayType.OFF -> Color(0xFF9CA3AF)
        DayType.HOLIDAY -> Color(0xFF3B82F6)
        DayType.SICK -> Color(0xFFEF4444)
    }
}

class WorkViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkUiState())
    val uiState: StateFlow<WorkUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentMonth = _uiState.value.currentMonth
            val startDate = currentMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDate = currentMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val today = LocalDate.now()
            val weekStart = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                weeklyHours = 38.0,
                overtimeHours = 2.0
            )
        }
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.minusMonths(1))
        loadData()
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.plusMonths(1))
        loadData()
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun startOvertimeTimer() {
        // Timer logic would be implemented here
    }
}
