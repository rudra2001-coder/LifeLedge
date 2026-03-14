package com.rudra.lifeledge.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.DayType
import com.rudra.lifeledge.data.local.entity.TransactionType
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

data class DashboardUiState(
    val lifeScore: Int = 0,
    val netBalance: Double = 0.0,
    val savingsBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlySaved: Double = 0.0,
    val savingsRate: Double = 0.0,
    val weeklyWorkHours: Double = 0.0,
    val workLifeBalance: Int = 0,
    val activeHabits: Int = 0,
    val habitsCompletedToday: Int = 0,
    val longestStreak: Int = 0,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val activeGoals: List<GoalUi> = emptyList(),
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
