package com.rudra.lifeledge.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.rudra.lifeledge.data.local.entity.ActivityLog
import com.rudra.lifeledge.data.local.entity.ActivityType
import com.rudra.lifeledge.data.repository.ActivityLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ReportType(val title: String, val icon: ImageVector) {
    DAILY("Daily", Icons.Default.Today),
    WEEKLY("Weekly", Icons.Default.DateRange),
    MONTHLY("Monthly", Icons.Default.CalendarMonth),
    YEARLY("Yearly", Icons.Default.Assessment)
}

data class ReportsUiState(
    val selectedReportType: ReportType = ReportType.WEEKLY,
    val activityLogs: List<ActivityLog> = emptyList(),
    val todayActivityCount: Int = 0,
    val weekActivityCount: Int = 0,
    val monthActivityCount: Int = 0,
    val selectedFilter: ActivityFilter = ActivityFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

enum class ActivityFilter(val title: String) {
    ALL("All"),
    FINANCE("Finance"),
    HABITS("Habits"),
    GOALS("Goals"),
    JOURNAL("Journal")
}

class ReportsViewModel(
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val today = LocalDate.now()
            val weekStart = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val monthStart = today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

            launch {
                activityLogRepository.getActivityLogs(100, 0).collect { logs ->
                    _uiState.value = _uiState.value.copy(
                        activityLogs = filterLogs(logs),
                        isLoading = false
                    )
                }
            }

            launch {
                activityLogRepository.getActivityCountForDate(todayStr).collect { count ->
                    _uiState.value = _uiState.value.copy(todayActivityCount = count)
                }
            }

            launch {
                activityLogRepository.getActivityCountBetween(weekStart, todayStr).collect { count ->
                    _uiState.value = _uiState.value.copy(weekActivityCount = count)
                }
            }

            launch {
                activityLogRepository.getActivityCountBetween(monthStart, todayStr).collect { count ->
                    _uiState.value = _uiState.value.copy(monthActivityCount = count)
                }
            }
        }
    }

    private fun filterLogs(logs: List<ActivityLog>): List<ActivityLog> {
        val filter = _uiState.value.selectedFilter
        val query = _uiState.value.searchQuery
        
        var filtered = logs
        
        filtered = when (filter) {
            ActivityFilter.ALL -> filtered
            ActivityFilter.FINANCE -> filtered.filter { 
                it.type == ActivityType.TRANSACTION || 
                it.type == ActivityType.RECURRING_TRANSACTION ||
                it.type == ActivityType.SAVING_ADDED ||
                it.type == ActivityType.SAVING_WITHDRAWN
            }
            ActivityFilter.HABITS -> filtered.filter { 
                it.type == ActivityType.HABIT_COMPLETED 
            }
            ActivityFilter.GOALS -> filtered.filter { 
                it.type == ActivityType.GOAL_CREATED || 
                it.type == ActivityType.GOAL_COMPLETED 
            }
            ActivityFilter.JOURNAL -> filtered.filter { 
                it.type == ActivityType.JOURNAL_ENTRY 
            }
        }
        
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        return filtered
    }

    fun selectReportType(type: ReportType) {
        _uiState.value = _uiState.value.copy(selectedReportType = type)
    }

    fun setFilter(filter: ActivityFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadData()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadData()
    }
}
