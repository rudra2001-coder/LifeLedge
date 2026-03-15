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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

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
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val workRepository: WorkRepository,
    private val financeRepository: FinanceRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val today = LocalDate.now()
                val monthStart = today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val monthEnd = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val weekStart = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Load work data
                val weeklyHours = workRepository.getTotalWorkHours(weekStart, monthEnd)

                // Daily Income/Expense/Balance
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                launch {
                    financeRepository.getDailyIncome(todayStr).collect { income ->
                        _uiState.value = _uiState.value.copy(dailyIncome = income)
                        _uiState.value = _uiState.value.copy(
                            dailyNetBalance = _uiState.value.dailyIncome - _uiState.value.dailyExpense
                        )
                    }
                }
                launch {
                    financeRepository.getDailyExpense(todayStr).collect { expense ->
                        _uiState.value = _uiState.value.copy(dailyExpense = expense)
                        _uiState.value = _uiState.value.copy(
                            dailyNetBalance = _uiState.value.dailyIncome - _uiState.value.dailyExpense
                        )
                    }
                }

                // Monthly Expense Categories (Top 7)
                launch {
                    financeRepository.getExpensesByCategory(monthStart, monthEnd).collect { categories ->
                        val categoryNames = mapOf(
                            1L to "Food", 2L to "Transport", 3L to "Shopping",
                            4L to "Bills", 5L to "Entertainment", 6L to "Health", 7L to "Others"
                        )
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

                // Weekly Work Days (Friday to Thursday)
                val friday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))
                val thursday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY))
                val weekDates = (0..6).map { friday.plusDays(it.toLong()) }
                val weekStartEpoch = friday.toEpochDay()
                val weekEndEpoch = thursday.toEpochDay()
                
                launch {
                    workRepository.getWorkLogsBetween(weekStartEpoch, weekEndEpoch).collect { workLogs ->
                        val workLogsMap = workLogs.associateBy { it.date }
                        val dayNames = listOf("Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
                        val weeklyDays = weekDates.mapIndexed { index, date ->
                            val dateEpoch = date.toEpochDay()
                            val workLog = workLogsMap[dateEpoch]
                            WorkDayUi(
                                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                dayName = dayNames.getOrElse(index) { "" },
                                isWorkingDay = workLog?.type == WorkType.WORK,
                                hoursWorked = if (workLog?.type == WorkType.WORK) 8.0 else 0.0
                            )
                        }
                        _uiState.value = _uiState.value.copy(weeklyWorkDays = weeklyDays)
                    }
                }

                // Monthly Work Summary
                val monthStartEpoch = today.withDayOfMonth(1).toEpochDay()
                val monthEndEpoch = today.toEpochDay()
                val workDaysCount = workRepository.getWorkDayCount(monthStartEpoch, monthEndEpoch)
                val extraHours = workRepository.getTotalExtraHours(monthStartEpoch, monthEndEpoch)
                _uiState.value = _uiState.value.copy(
                    monthlyWorkDays = workDaysCount,
                    monthlyExtraHours = extraHours
                )

                // Load habit data
                val activeHabitsTotal = habitRepository.getActiveHabits().firstOrNull()?.size ?: 0
                val habitsCompleted = habitRepository.getCompletionsForDate(today.format(DateTimeFormatter.ISO_LOCAL_DATE)).firstOrNull()?.size ?: 0

                _uiState.value = _uiState.value.copy(
                    weeklyWorkHours = weeklyHours,
                    workLifeBalance = calculateWorkLifeBalance(weeklyHours),
                    activeHabits = activeHabitsTotal,
                    habitsCompletedToday = habitsCompleted,
                    isLoading = false
                )

                // Reactive balance calculations - Money Flow Model
                // Net Balance = Income - Expense - Saved + TransferredFromSavings
                launch {
                    financeRepository.getNetBalance().collect { balance ->
                        _uiState.value = _uiState.value.copy(netBalance = balance)
                        _uiState.value = _uiState.value.copy(lifeScore = calculateLifeScore())
                    }
                }

                // Monthly Income
                launch {
                    financeRepository.getMonthlyIncome(monthStart, monthEnd).collect { income ->
                        _uiState.value = _uiState.value.copy(monthlyIncome = income)
                    }
                }

                // Monthly Expense
                launch {
                    financeRepository.getMonthlyExpense(monthStart, monthEnd).collect { expense ->
                        _uiState.value = _uiState.value.copy(monthlyExpense = expense)
                        val income = _uiState.value.monthlyIncome
                        val savingsRate = if (income > 0) ((income - expense) / income) * 100 else 0.0
                        _uiState.value = _uiState.value.copy(savingsRate = savingsRate)
                    }
                }

                // Savings Balance = Total Saved - Total Transferred From Savings
                launch {
                    financeRepository.getSavingsBalance().collect { savings ->
                        _uiState.value = _uiState.value.copy(savingsBalance = savings)
                    }
                }

                // Monthly Saved
                launch {
                    financeRepository.getMonthlySaved(monthStart, monthEnd).collect { saved ->
                        _uiState.value = _uiState.value.copy(monthlySaved = saved)
                    }
                }

                launch {
                    financeRepository.getRecentTransactions(5).collect { txs ->
                        val txUis = txs.map { tx ->
                            TransactionUi(
                                id = tx.id,
                                category = "Transaction",
                                amount = tx.amount,
                                date = tx.date,
                                isExpense = tx.type == TransactionType.EXPENSE
                            )
                        }
                        _uiState.value = _uiState.value.copy(recentTransactions = txUis)
                    }
                }

                // Upcoming Recurring Transactions
                launch {
                    financeRepository.getActiveRecurringTransactions().collect { recurring ->
                        val upcoming = recurring
                            .filter { it.isActive }
                            .sortedBy { it.nextDate }
                            .take(5)
                        _uiState.value = _uiState.value.copy(upcomingRecurring = upcoming)
                    }
                }

                launch {
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
                        _uiState.value = _uiState.value.copy(lifeScore = calculateLifeScore())
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateLifeScore(): Int {
        val financial = calculateFinancialScore()
        val work = calculateWorkScore()
        val habits = calculateHabitScore()
        val goals = calculateGoalScore()
        return ((financial * 0.30 + work * 0.25 + habits * 0.25 + goals * 0.20)).toInt()
    }

    private fun calculateFinancialScore(): Double {
        val savingsRate = _uiState.value.savingsRate
        return when {
            savingsRate >= 20 -> 100.0
            savingsRate >= 15 -> 80.0
            savingsRate >= 10 -> 60.0
            savingsRate >= 5 -> 40.0
            else -> 20.0
        }
    }

    private fun calculateWorkScore(): Double {
        val hours = _uiState.value.weeklyWorkHours
        return when {
            hours in 35.0..45.0 -> 100.0
            hours in 30.0..50.0 -> 80.0
            hours in 25.0..55.0 -> 60.0
            else -> 40.0
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
        val avgProgress = goals.map { (it.current / it.target).coerceIn(0.0, 1.0) }.average()
        return avgProgress * 100
    }

    private fun calculateWorkLifeBalance(weeklyHours: Double): Int {
        return when {
            weeklyHours <= 40 -> 100
            weeklyHours <= 45 -> 80
            weeklyHours <= 50 -> 60
            weeklyHours <= 55 -> 40
            else -> 20
        }
    }
}
