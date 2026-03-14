package com.rudra.lifeledge.ui.screens.work

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.data.local.entity.WorkLog
import com.rudra.lifeledge.data.local.entity.WorkType
import com.rudra.lifeledge.data.repository.WorkRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

private fun LocalDate.toEpochMilli(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

data class WorkUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val workLogs: Map<Long, WorkLog> = emptyMap(),
    val recentLogs: List<WorkLog> = emptyList(),
    val selectedDate: LocalDate? = null,
    val showMarkDialog: Boolean = false,
    val isBulkMode: Boolean = false,
    val selectedDates: Set<LocalDate> = emptySet(),
    val workDaysThisMonth: Int = 0,
    val offDaysThisMonth: Int = 0,
    val holidaysThisMonth: Int = 0,
    val extraHoursThisMonth: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(navController: NavController) {
    val viewModel: WorkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showMarkDialog && uiState.selectedDate != null) {
        MarkDayDialog(
            date = uiState.selectedDate!!,
            existingLog = uiState.workLogs[uiState.selectedDate!!.toEpochMilli()],
            isBulkMode = uiState.isBulkMode,
            selectedCount = uiState.selectedDates.size,
            onDismiss = { viewModel.hideMarkDialog() },
            onConfirm = { type, extraHours, note ->
                if (uiState.isBulkMode) {
                    viewModel.markBulkDates(type, extraHours, note)
                } else {
                    viewModel.markDate(uiState.selectedDate!!, type, extraHours, note)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Center", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.toggleBulkMode() }) {
                        Icon(
                            if (uiState.isBulkMode) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                            contentDescription = "Bulk Mode"
                        )
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                WorkStatsCard(
                    workDays = uiState.workDaysThisMonth,
                    offDays = uiState.offDaysThisMonth,
                    holidays = uiState.holidaysThisMonth,
                    extraHours = uiState.extraHoursThisMonth
                )
            }

            item {
                StreakCard(
                    currentStreak = uiState.currentStreak,
                    bestStreak = uiState.bestStreak
                )
            }

            item {
                CalendarCard(
                    currentMonth = uiState.currentMonth,
                    workLogs = uiState.workLogs,
                    selectedDate = uiState.selectedDate,
                    isBulkMode = uiState.isBulkMode,
                    selectedDates = uiState.selectedDates,
                    onDateClick = { date ->
                        if (uiState.isBulkMode) {
                            viewModel.toggleDateSelection(date)
                        } else {
                            viewModel.selectDate(date)
                        }
                    },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            item {
                Text(
                    "Recent Work Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.recentLogs.isEmpty()) {
                item {
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
                                "No work logs yet. Tap a date to mark it!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentLogs.take(15)) { log ->
                    WorkLogItem(log = log, onClick = {
                        val date = LocalDate.ofEpochDay(log.date / (24 * 60 * 60 * 1000))
                        viewModel.selectDate(date)
                    })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WorkStatsCard(workDays: Int, offDays: Int, holidays: Int, extraHours: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Work", value = workDays.toString(), color = Success)
                StatItem(label = "Off", value = offDays.toString(), color = Error)
                StatItem(label = "Holiday", value = holidays.toString(), color = Warning)
                StatItem(label = "Extra", value = "${extraHours}h", color = Color(0xFF06B6D4))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun StreakCard(currentStreak: Int, bestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Accent, modifier = Modifier.size(32.dp))
                Text("$currentStreak days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Current Streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                Text("$bestStreak days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Best Streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CalendarCard(
    currentMonth: YearMonth,
    workLogs: Map<Long, WorkLog>,
    selectedDate: LocalDate?,
    isBulkMode: Boolean,
    selectedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous") }
                Text(
                    "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val totalCells = startDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayIndex = row * 7 + col - startDayOfWeek + 1
                        if (dayIndex in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayIndex)
                            val epochMilli = date.toEpochMilli()
                            val log = workLogs[epochMilli]
                            val isSelected = selectedDate == date || selectedDates.contains(date)
                            val isToday = date == LocalDate.now()

                            CalendarDay(
                                day = dayIndex,
                                workType = log?.type,
                                extraHours = log?.extraHours ?: 0,
                                isSelected = isSelected,
                                isToday = isToday,
                                onClick = { onDateClick(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LegendItem(color = Success, label = "Work")
                LegendItem(color = Error, label = "Off")
                LegendItem(color = Warning, label = "Holiday")
                LegendItem(color = Color(0xFF06B6D4), label = "Extra")
            }
        }
    }
}

@Composable
fun CalendarDay(day: Int, workType: WorkType?, extraHours: Int, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor = when (workType) {
        WorkType.WORK -> Success.copy(alpha = 0.8f)
        WorkType.OFF -> Error.copy(alpha = 0.6f)
        WorkType.HOLIDAY -> Warning.copy(alpha = 0.7f)
        null -> Color.Transparent
    }
    val textColor = when {
        workType != null -> Color.White
        isToday -> Primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier.aspectRatio(1f).padding(2.dp).clip(RoundedCornerShape(8.dp)).background(backgroundColor)
            .then(if (isSelected) Modifier.border(2.dp, Primary, RoundedCornerShape(8.dp)) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = textColor)
            if (extraHours > 0 && workType == WorkType.WORK) {
                Text("+${extraHours}h", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun WorkLogItem(log: WorkLog, onClick: () -> Unit) {
    val date = LocalDate.ofEpochDay(log.date / (24 * 60 * 60 * 1000))
    val formatter = DateTimeFormatter.ofPattern("MMM dd")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(
                        when (log.type) {
                            WorkType.WORK -> Success.copy(alpha = 0.15f)
                            WorkType.OFF -> Error.copy(alpha = 0.15f)
                            WorkType.HOLIDAY -> Warning.copy(alpha = 0.15f)
                        }, CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (log.type) {
                            WorkType.WORK -> Icons.Default.Work
                            WorkType.OFF -> Icons.Default.Hotel
                            WorkType.HOLIDAY -> Icons.Default.Celebration
                        },
                        contentDescription = null,
                        tint = when (log.type) {
                            WorkType.WORK -> Success
                            WorkType.OFF -> Error
                            WorkType.HOLIDAY -> Warning
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(date.format(formatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        when (log.type) {
                            WorkType.WORK -> "Work Day"
                            WorkType.OFF -> "Off Day"
                            WorkType.HOLIDAY -> "Holiday"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (log.extraHours > 0) {
                Text("+${log.extraHours}h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDayDialog(
    date: LocalDate,
    existingLog: WorkLog?,
    isBulkMode: Boolean,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (WorkType, Int, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(existingLog?.type ?: WorkType.WORK) }
    var extraHours by remember { mutableStateOf(existingLog?.extraHours?.toString() ?: "0") }
    var note by remember { mutableStateOf(existingLog?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBulkMode) "Mark $selectedCount Dates" else "Mark ${date.format(DateTimeFormatter.ofPattern("MMM dd"))}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                WorkType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedType == type) when (type) {
                                    WorkType.WORK -> Success.copy(alpha = 0.15f)
                                    WorkType.OFF -> Error.copy(alpha = 0.15f)
                                    WorkType.HOLIDAY -> Warning.copy(alpha = 0.15f)
                                } else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedType = type }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            when (type) {
                                WorkType.WORK -> Icons.Default.Work
                                WorkType.OFF -> Icons.Default.Hotel
                                WorkType.HOLIDAY -> Icons.Default.Celebration
                            },
                            contentDescription = null,
                            tint = when (type) {
                                WorkType.WORK -> Success
                                WorkType.OFF -> Error
                                WorkType.HOLIDAY -> Warning
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(when (type) {
                            WorkType.WORK -> "Work Day"
                            WorkType.OFF -> "Off Day"
                            WorkType.HOLIDAY -> "Holiday"
                        })
                    }
                }
                if (selectedType == WorkType.WORK) {
                    OutlinedTextField(
                        value = extraHours,
                        onValueChange = { if (it.all { c -> c.isDigit() }) extraHours = it },
                        label = { Text("Extra Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedType, extraHours.toIntOrNull() ?: 0, note.ifBlank { null }) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

class WorkViewModel(private val workRepository: WorkRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkUiState())
    val uiState: StateFlow<WorkUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val monthStart = today.withDayOfMonth(1)
            val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
            val startMilli = monthStart.toEpochMilli()
            val endMilli = monthEnd.toEpochMilli()

            combine(
                workRepository.getWorkLogsBetween(startMilli, endMilli),
                workRepository.getRecentWorkLogs(100)
            ) { monthLogsList, recentLogs ->
                val logsMap = monthLogsList.associateBy { it.date }
                val recentList = recentLogs.sortedByDescending { it.date }
                Triple(logsMap, recentList, calculateStreak(recentList))
            }.collect { (logsMap, recentLogs, streak) ->
                val monthLogsList = logsMap.values.toList()
                _uiState.value = _uiState.value.copy(
                    workLogs = logsMap,
                    recentLogs = recentLogs,
                    workDaysThisMonth = monthLogsList.count { it.type == WorkType.WORK },
                    offDaysThisMonth = monthLogsList.count { it.type == WorkType.OFF },
                    holidaysThisMonth = monthLogsList.count { it.type == WorkType.HOLIDAY },
                    extraHoursThisMonth = monthLogsList.sumOf { it.extraHours },
                    currentStreak = streak.first,
                    bestStreak = streak.second,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateStreak(logs: List<WorkLog>): Pair<Int, Int> {
        if (logs.isEmpty()) return Pair(0, 0)
        val workDates = logs.filter { it.type == WorkType.WORK }.map { LocalDate.ofEpochDay(it.date / (24 * 60 * 60 * 1000)) }.sortedDescending()
        if (workDates.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 1
        val today = LocalDate.now()
        var checkDate = today

        for (date in workDates) {
            if (date == checkDate || date == checkDate.minusDays(1)) {
                currentStreak++
                checkDate = date.minusDays(1)
            } else if (date.isBefore(checkDate.minusDays(1))) {
                break
            }
        }

        for (i in 1 until workDates.size) {
            if (workDates[i - 1].minusDays(1) == workDates[i]) tempStreak++ else {
                bestStreak = maxOf(bestStreak, tempStreak)
                tempStreak = 1
            }
        }
        bestStreak = maxOf(bestStreak, tempStreak, currentStreak)
        return Pair(currentStreak, bestStreak)
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date, showMarkDialog = true)
    }

    fun hideMarkDialog() {
        _uiState.value = _uiState.value.copy(showMarkDialog = false, selectedDate = null, isBulkMode = false, selectedDates = emptySet())
    }

    fun toggleBulkMode() {
        _uiState.value = _uiState.value.copy(isBulkMode = !_uiState.value.isBulkMode, selectedDates = emptySet())
    }

    fun toggleDateSelection(date: LocalDate) {
        val current = _uiState.value.selectedDates
        _uiState.value = _uiState.value.copy(selectedDates = if (current.contains(date)) current - date else current + date)
    }

    fun markDate(date: LocalDate, type: WorkType, extraHours: Int, note: String?) {
        viewModelScope.launch {
            workRepository.saveWorkLog(WorkLog(date = date.toEpochMilli(), type = type, extraHours = extraHours, note = note))
            hideMarkDialog()
        }
    }

    fun markBulkDates(type: WorkType, extraHours: Int, note: String?) {
        viewModelScope.launch {
            val logs = _uiState.value.selectedDates.map { date ->
                WorkLog(date = date.toEpochMilli(), type = type, extraHours = extraHours, note = note)
            }
            workRepository.saveWorkLogs(logs)
            hideMarkDialog()
        }
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.minusMonths(1))
        loadMonthData()
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.plusMonths(1))
        loadMonthData()
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            val month = _uiState.value.currentMonth
            val startMilli = month.atDay(1).toEpochMilli()
            val endMilli = month.atEndOfMonth().toEpochMilli()

            workRepository.getWorkLogsBetween(startMilli, endMilli).collect { logs ->
                val logsMap = logs.associateBy { it.date }
                _uiState.value = _uiState.value.copy(
                    workLogs = logsMap,
                    workDaysThisMonth = logs.count { it.type == WorkType.WORK },
                    offDaysThisMonth = logs.count { it.type == WorkType.OFF },
                    holidaysThisMonth = logs.count { it.type == WorkType.HOLIDAY },
                    extraHoursThisMonth = logs.sumOf { it.extraHours }
                )
            }
        }
    }
}
