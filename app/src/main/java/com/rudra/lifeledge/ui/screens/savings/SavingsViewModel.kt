package com.rudra.lifeledge.ui.screens.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.SavingGoal
import com.rudra.lifeledge.data.local.entity.SavingTransaction
import com.rudra.lifeledge.data.repository.FinanceRepository
import com.rudra.lifeledge.data.repository.SavingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SavingsUiState(
    val activeGoals: List<SavingGoal> = emptyList(),
    val completedGoals: List<SavingGoal> = emptyList(),
    val recentTransactions: List<SavingTransaction> = emptyList(),
    val totalSavings: Double = 0.0,
    val generalSavings: Double = 0.0,
    val selectedTab: Int = 0,
    val showAddGoalDialog: Boolean = false,
    val showAddSavingsDialog: Boolean = false,
    val showWithdrawDialog: Boolean = false,
    val withdrawingGoal: SavingGoal? = null,
    val isLoading: Boolean = true,
    val recentlyDeletedGoal: SavingGoal? = null
)


class SavingsViewModel(
    private val savingRepository: SavingRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                savingRepository.getActiveGoals(),
                savingRepository.getCompletedGoals(),
                savingRepository.getRecentTransactions(20),
                savingRepository.getTotalSavings(),
                savingRepository.getGeneralSavingsBalance()
            ) { activeGoals: List<SavingGoal>, completedGoals: List<SavingGoal>, transactions: List<SavingTransaction>, totalSavings: Double?, generalSavings: Double? ->
                SavingsUiState(
                    activeGoals = activeGoals,
                    completedGoals = completedGoals,
                    recentTransactions = transactions.take(20),
                    totalSavings = totalSavings ?: 0.0,
                    generalSavings = generalSavings ?: 0.0,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    selectedTab = _uiState.value.selectedTab,
                    showAddGoalDialog = _uiState.value.showAddGoalDialog,
                    showAddSavingsDialog = _uiState.value.showAddSavingsDialog
                )
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun showAddGoalDialog() {
        _uiState.value = _uiState.value.copy(showAddGoalDialog = true)
    }

    fun hideAddGoalDialog() {
        _uiState.value = _uiState.value.copy(showAddGoalDialog = false)
    }

    fun showAddSavingsDialog() {
        _uiState.value = _uiState.value.copy(showAddSavingsDialog = true)
    }

    fun hideAddSavingsDialog() {
        _uiState.value = _uiState.value.copy(showAddSavingsDialog = false)
    }

    fun showWithdrawDialog(goal: SavingGoal) {
        _uiState.value = _uiState.value.copy(showWithdrawDialog = true, withdrawingGoal = goal)
    }

    fun hideWithdrawDialog() {
        _uiState.value = _uiState.value.copy(showWithdrawDialog = false, withdrawingGoal = null)
    }

    fun createGoal(title: String, targetAmount: Double, priority: String, icon: String, color: Long) {
        viewModelScope.launch {
            savingRepository.addGoal(title, targetAmount, priority, icon, color)
            hideAddGoalDialog()
        }
    }

    fun addSaving(amount: Double, goalId: Long?, note: String?, source: String = "CASH") {
        viewModelScope.launch {
            // Create transaction that reduces net balance and increases savings
            financeRepository.addSaving(amount, note)
            // Also add to goal-specific savings if applicable
            savingRepository.addSaving(amount, goalId, note, source)
            hideAddSavingsDialog()
        }
    }

    fun withdrawFromGoal(amount: Double, goalId: Long, note: String?) {
        viewModelScope.launch {
            savingRepository.withdrawFromGoal(amount, goalId, note)
            hideWithdrawDialog()
        }
    }

    fun deleteGoal(goal: SavingGoal) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedGoal = goal)
            savingRepository.deleteGoal(goal)
        }
    }

    fun undoDeleteGoal() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedGoal?.let { goal ->
                savingRepository.createGoal(goal)
                _uiState.value = _uiState.value.copy(recentlyDeletedGoal = null)
            }
        }
    }

    fun clearDeletedGoal() {
        _uiState.value = _uiState.value.copy(recentlyDeletedGoal = null)
    }
}
