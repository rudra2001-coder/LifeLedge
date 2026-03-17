package com.rudra.lifeledge.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.DayType
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.local.entity.WorkType
import com.rudra.lifeledge.data.repository.FinanceRepository
import com.rudra.lifeledge.data.repository.GoalRepository
import com.rudra.lifeledge.data.repository.HabitRepository
import com.rudra.lifeledge.data.repository.WorkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.min

data class DashboardUiState(
    val lifeScore: Int = 0,
    val netBalance: Double = 0.0,
    val savingsBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlySaved: Double = 0.0,
    val savingsRate: Double = 0.0,
    val dailyIncome: Double = 0.0,
    val dailyExpense: Double = 0.0,
    val dailyNetBalance: Double = 0.0,
    val expenseCategories: List<ExpenseCategoryUi> = emptyList(),
    val weeklyWorkDays: List<WorkDayUi> = emptyList(),
    val monthlyWorkDays: Int = 0,
    val monthlyExtraHours: Int = 0,
    val weeklyWorkHours: Double = 0.0,
    val workLifeBalance: Int = 0,
    val activeHabits: Int = 0,
    val habitsCompletedToday: Int = 0,
    val longestStreak: Int = 0,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val activeGoals: List<GoalUi> = emptyList(),
    val upcomingRecurring: List<RecurringTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val lastUpdated: LocalDate? = null
)

data class ExpenseCategoryUi(
    val categoryId: Long,
    val categoryName: String,
    val total: Double
)

data class WorkDayUi(
    val date: String,
    val dayName: String,
    val isWorkingDay: Boolean,
    val hoursWorked: Double
)

data class TransactionUi(
    val id: Long,
    val category: String,
    val amount: Double,
    val date: String,
    val isExpense: Boolean
)

data class GoalUi(
    val id: Long,
    val title: String,
    val current: Double,
    val target: Double,
    val unit: String,
    val color: Int
)

class DashboardViewModel(
    private val workRepository: WorkRepository,
    private val financeRepository: FinanceRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val today = LocalDate.now()
    private val monthStart = today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val monthEnd = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val weekStart = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        loadData()
        setupRealTimeUpdates()
    }

    private fun setupRealTimeUpdates() {
        viewModelScope.launch {
            combine(
                financeRepository.getNetBalance(),
                financeRepository.getDailyIncome(todayStr),
                financeRepository.getDailyExpense(todayStr),
                financeRepository.getMonthlyIncome(monthStart, monthEnd),
                financeRepository.getMonthlyExpense(monthStart, monthEnd)
            ) { netBalance, dailyIncome, dailyExpense, monthlyIncome, monthlyExpense ->
                val dailyNetBalance = dailyIncome - dailyExpense
                val savingsRate = if (monthlyIncome > 0)
                    ((monthlyIncome - monthlyExpense) / monthlyIncome) * 100
                else 0.0

                _uiState.value = _uiState.value.copy(
                    netBalance = netBalance,
                    dailyIncome = dailyIncome,
                    dailyExpense = dailyExpense,
                    dailyNetBalance = dailyNetBalance,
                    monthlyIncome = monthlyIncome,
                    monthlyExpense = monthlyExpense,
                    savingsRate = savingsRate
                )
                updateLifeScore()
            }.collect {}
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Launch these as separate collectors so they don't block each other
                loadWorkData()
                loadHabitData()
                loadFinancialData()
                loadGoalData()

                // Give it a small delay to allow first emissions to process
                delay(500)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastUpdated = LocalDate.now()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500) // Minimum refresh animation
            loadData()
            _isRefreshing.value = false
        }
    }

    private fun loadWorkData() {
        viewModelScope.launch {
            try {
                val weekStartEpoch = today.minusDays(7).toEpochDay()
                val monthEndEpoch = today.toEpochDay()
                val weekStartStr = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val monthEndStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Weekly hours
                val weeklyHours = workRepository.getTotalWorkHours(weekStartStr, monthEndStr)

                // Weekly calendar
                val friday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))
                val thursday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
                val weekDates = (0..6).map { friday.plusDays(it.toLong()) }
                val weekStartEpochCal = friday.toEpochDay()
                val weekEndEpoch = thursday.toEpochDay()

                workRepository.getWorkLogsBetween(weekStartEpochCal, weekEndEpoch).collect { workLogs ->
                    val workLogsMap = workLogs.associateBy { it.date }
                    val dayNames = listOf("Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
                    val weeklyDays = weekDates.mapIndexed { index, date ->
                        val dateEpoch = date.toEpochDay()
                        val workLog = workLogsMap[dateEpoch]
                        WorkDayUi(
                            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            dayName = dayNames.getOrElse(index) { "" },
                            isWorkingDay = workLog?.type == WorkType.WORK,
                            hoursWorked = (workLog?.extraHours ?: 0).toDouble()
                        )
                    }

                    // Monthly summary
                    val monthStartEpoch = today.withDayOfMonth(1).toEpochDay()
                    val workDaysCount = workRepository.getWorkDayCount(monthStartEpoch, monthEndEpoch)
                    val extraHours = workRepository.getTotalExtraHours(monthStartEpoch, monthEndEpoch)

                    _uiState.value = _uiState.value.copy(
                        weeklyWorkHours = weeklyHours,
                        weeklyWorkDays = weeklyDays,
                        monthlyWorkDays = workDaysCount,
                        monthlyExtraHours = extraHours,
                        workLifeBalance = calculateWorkLifeBalance(weeklyHours)
                    )
                    updateLifeScore()
                }
            } catch (e: Exception) {
                // Handle error but don't crash
            }
        }
    }

    private fun loadHabitData() {
        viewModelScope.launch {
            try {
                habitRepository.getActiveHabits().collect { activeHabits ->
                    val activeHabitsTotal = activeHabits.size
                    val habitsCompleted = habitRepository.getCompletionsForDate(todayStr).firstOrNull()?.size ?: 0
                    val longestStreak = habitRepository.getLongestStreak().firstOrNull() ?: 0

                    _uiState.value = _uiState.value.copy(
                        activeHabits = activeHabitsTotal,
                        habitsCompletedToday = habitsCompleted,
                        longestStreak = longestStreak
                    )
                    updateLifeScore()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadFinancialData() {
        try {
            // Expense Categories
            viewModelScope.launch {
                financeRepository.getExpensesByCategory(monthStart, monthEnd).collect { categories ->
                    val categoryNames = getCategoryNames()
                    val expenseCats = categories
                        .sortedByDescending { it.total }
                        .take(7)
                        .map { cat ->
                            ExpenseCategoryUi(
                                categoryId = cat.categoryId,
                                categoryName = categoryNames[cat.categoryId] ?: "Category ${cat.categoryId}",
                                total = cat.total
                            )
                        }
                    _uiState.value = _uiState.value.copy(expenseCategories = expenseCats)
                }
            }

            // Recent Transactions
            viewModelScope.launch {
                financeRepository.getRecentTransactions(10).collect { txs ->
                    val txUis = txs.map { tx ->
                        TransactionUi(
                            id = tx.id,
                            category = getTransactionCategory(tx.categoryId),
                            amount = tx.amount,
                            date = formatTransactionDate(tx.date),
                            isExpense = tx.type == TransactionType.EXPENSE
                        )
                    }
                    _uiState.value = _uiState.value.copy(recentTransactions = txUis)
                }
            }

            // Upcoming Recurring
            viewModelScope.launch {
                financeRepository.getActiveRecurringTransactions().collect { recurring ->
                    val upcoming = recurring
                        .filter { it.isActive }
                        .sortedBy { it.nextDate }
                        .take(5)
                    _uiState.value = _uiState.value.copy(upcomingRecurring = upcoming)
                }
            }

            // Savings Balance
            viewModelScope.launch {
                financeRepository.getSavingsBalance().collect { savings ->
                    _uiState.value = _uiState.value.copy(savingsBalance = savings)
                }
            }

            // Monthly Saved
            viewModelScope.launch {
                financeRepository.getMonthlySaved(monthStart, monthEnd).collect { saved ->
                    _uiState.value = _uiState.value.copy(monthlySaved = saved)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun loadGoalData() {
        viewModelScope.launch {
            try {
                goalRepository.getActiveGoals().collect { goals ->
                    val goalUis = goals.take(5).map { goal ->
                        GoalUi(
                            id = goal.id,
                            title = goal.title,
                            current = goal.currentValue,
                            target = goal.targetValue,
                            unit = goal.unit,
                            color = goal.color
                        )
                    }
                    _uiState.value = _uiState.value.copy(activeGoals = goalUis)
                    updateLifeScore()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun updateLifeScore() {
        val financial = calculateFinancialScore()
        val work = calculateWorkScore()
        val habits = calculateHabitScore()
        val goals = calculateGoalScore()

        val score = ((financial * 0.30 + work * 0.25 + habits * 0.25 + goals * 0.20)).toInt()
            .coerceIn(0, 100)

        _uiState.value = _uiState.value.copy(lifeScore = score)
    }

    private fun calculateFinancialScore(): Double {
        val savingsRate = _uiState.value.savingsRate
        val netBalance = _uiState.value.netBalance

        return when {
            savingsRate >= 20 && netBalance > 0 -> 100.0
            savingsRate >= 15 -> 85.0
            savingsRate >= 10 -> 70.0
            savingsRate >= 5 -> 50.0
            savingsRate > 0 -> 30.0
            else -> 15.0
        }
    }

    private fun calculateWorkScore(): Double {
        val hours = _uiState.value.weeklyWorkHours
        return when {
            hours in 35.0..45.0 -> 100.0
            hours in 30.0..50.0 -> 85.0
            hours in 25.0..55.0 -> 65.0
            hours in 20.0..60.0 -> 45.0
            else -> 25.0
        }
    }

    private fun calculateHabitScore(): Double {
        val completed = _uiState.value.habitsCompletedToday
        val total = _uiState.value.activeHabits
        return if (total > 0) (completed.toDouble() / total) * 100 else 0.0
    }

    private fun calculateGoalScore(): Double {
        val goals = _uiState.value.activeGoals
        if (goals.isEmpty()) return 50.0

        val weightedProgress = goals.map { goal ->
            val progress = (goal.current / goal.target).coerceIn(0.0, 1.0)
            // Give more weight to goals closer to completion
            progress * (1 + progress) / 2
        }.average()

        return weightedProgress * 100
    }

    private fun calculateWorkLifeBalance(weeklyHours: Double): Int {
        return when {
            weeklyHours <= 38 -> 100
            weeklyHours <= 42 -> 90
            weeklyHours <= 45 -> 75
            weeklyHours <= 48 -> 60
            weeklyHours <= 52 -> 45
            weeklyHours <= 56 -> 30
            else -> 15
        }.coerceIn(0, 100)
    }

    private fun getCategoryNames(): Map<Long, String> {
        return mapOf(
            1L to "Food & Dining",
            2L to "Transport",
            3L to "Shopping",
            4L to "Bills & Utilities",
            5L to "Entertainment",
            6L to "Health",
            7L to "Education",
            8L to "Personal Care",
            9L to "Gifts & Donations",
            10L to "Other"
        )
    }

    private fun getTransactionCategory(categoryId: Long?): String {
        return when (categoryId) {
            1L -> "Food"
            2L -> "Transport"
            3L -> "Shopping"
            4L -> "Bills"
            5L -> "Entertainment"
            6L -> "Health"
            7L -> "Education"
            8L -> "Personal"
            9L -> "Gifts"
            else -> "Other"
        }
    }

    private fun formatTransactionDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}