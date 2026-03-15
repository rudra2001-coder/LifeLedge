package com.rudra.lifeledge.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.ActivityLog
import com.rudra.lifeledge.data.local.entity.ActivityType
import com.rudra.lifeledge.data.repository.ActivityLogRepository
import com.rudra.lifeledge.data.repository.FinanceRepository
import com.rudra.lifeledge.data.repository.HabitRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val datesWithActivities: Map<LocalDate, List<ActivityType>> = emptyMap(),
    val selectedDateActivities: List<ActivityLog> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(
    private val activityLogRepository: ActivityLogRepository,
    private val financeRepository: FinanceRepository,
    private val habitRepository: HabitRepository
) : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthActivities()
    }

    fun loadMonthActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val currentMonth = _uiState.value.currentMonth
            val startDate = currentMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDate = currentMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            activityLogRepository.getActivityLogsBetween(startDate, endDate).collect { logs ->
                val activitiesByDate = logs.groupBy { LocalDate.parse(it.date) }
                _uiState.value = _uiState.value.copy(
                    datesWithActivities = activitiesByDate.mapValues { (_, logs) -> logs.map { it.type }.distinct() },
                    isLoading = false
                )
            }
        }
    }

    fun loadDateActivities(date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            activityLogRepository.getActivityLogsForDate(dateStr).collect { logs ->
                _uiState.value = _uiState.value.copy(
                    selectedDate = date,
                    selectedDateActivities = logs
                )
            }
        }
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.plusMonths(1))
        loadMonthActivities()
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.minusMonths(1))
        loadMonthActivities()
    }

    fun selectDate(date: LocalDate) {
        loadDateActivities(date)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            MonthSelector(
                currentMonth = uiState.currentMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalendarGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                datesWithActivities = uiState.datesWithActivities,
                onDateSelected = { viewModel.selectDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DayActivitiesList(
                selectedDate = uiState.selectedDate,
                activities = uiState.selectedDateActivities
            )
        }
    }
}

@Composable
fun MonthSelector(
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
            style = MaterialTheme.typography.titleLarge,
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
    selectedDate: LocalDate,
    datesWithActivities: Map<LocalDate, List<ActivityType>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
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

        val totalCells = startOffset + lastDayOfMonth.dayOfMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - startOffset + 1
                    if (dayIndex in 1..lastDayOfMonth.dayOfMonth) {
                        val date = currentMonth.atDay(dayIndex)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val activities = datesWithActivities[date] ?: emptyList()

                        CalendarDay(
                            day = dayIndex,
                            isSelected = isSelected,
                            isToday = isToday,
                            activities = activities,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    activities: List<ActivityType>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> Primary
                    isToday -> Primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSelected -> Color.White
                isToday -> Primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        if (activities.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                activities.take(3).forEach { type ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(getActivityColor(type), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun DayActivitiesList(
    selectedDate: LocalDate,
    activities: List<ActivityLog>
) {
    Column {
        Text(
            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No activities on this day",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activities) { activity ->
                    ActivityLogCard(activity = activity)
                }
            }
        }
    }
}

@Composable
fun ActivityLogCard(activity: ActivityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(activity.color).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getActivityIcon(activity.type),
                    contentDescription = null,
                    tint = Color(activity.color),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                activity.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            activity.amount?.let { amount ->
                Text(
                    "৳${String.format("%.0f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(activity.color)
                )
            }
        }
    }
}

fun getActivityColor(type: ActivityType): Color {
    return when (type) {
        ActivityType.TRANSACTION -> Success
        ActivityType.RECURRING_TRANSACTION -> Primary
        ActivityType.HABIT_COMPLETED -> Secondary
        ActivityType.GOAL_CREATED, ActivityType.GOAL_COMPLETED -> Warning
        ActivityType.SAVING_ADDED, ActivityType.SAVING_WITHDRAWN -> Success
        ActivityType.JOURNAL_ENTRY -> Color(0xFF8B5CF6)
        ActivityType.CARD_ADDED -> Color(0xFFEC4899)
        ActivityType.WORK_LOG -> Color(0xFF3B82F6)
    }
}

fun getActivityIcon(type: ActivityType) = when (type) {
    ActivityType.TRANSACTION -> Icons.Default.ArrowDownward
    ActivityType.RECURRING_TRANSACTION -> Icons.Default.Repeat
    ActivityType.HABIT_COMPLETED -> Icons.Default.CheckCircle
    ActivityType.GOAL_CREATED, ActivityType.GOAL_COMPLETED -> Icons.Default.Flag
    ActivityType.SAVING_ADDED, ActivityType.SAVING_WITHDRAWN -> Icons.Default.Savings
    ActivityType.JOURNAL_ENTRY -> Icons.Default.Book
    ActivityType.CARD_ADDED -> Icons.Default.CreditCard
    ActivityType.WORK_LOG -> Icons.Default.Work
}
