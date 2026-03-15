package com.rudra.lifeledge.ui.screens.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.WorkLog
import com.rudra.lifeledge.data.local.entity.WorkType
import com.rudra.lifeledge.data.repository.WorkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

fun LocalDate.toEpochMilli(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

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
