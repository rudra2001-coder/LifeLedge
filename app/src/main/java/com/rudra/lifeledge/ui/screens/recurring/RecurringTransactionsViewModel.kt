package com.rudra.lifeledge.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecurringTransactionsUiState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val totalMonthlyIncome: Double = 0.0,
    val totalMonthlyExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val editingRecurring: RecurringTransaction? = null
)

class RecurringTransactionsViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecurringTransactionsUiState())
    val uiState: StateFlow<RecurringTransactionsUiState> = _uiState.asStateFlow()

    fun loadRecurringTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            financeRepository.getAllRecurringTransactions().collect { transactions ->
                val activeTransactions = transactions.filter { rec -> rec.isActive }
                var totalIncome = 0.0
                var totalExpense = 0.0
                for (t in activeTransactions) {
                    if (t.type == TransactionType.INCOME) {
                        totalIncome += t.amount
                    } else if (t.type == TransactionType.EXPENSE) {
                        totalExpense += t.amount
                    }
                }

                _uiState.value = _uiState.value.copy(
                    recurringTransactions = transactions,
                    totalMonthlyIncome = totalIncome,
                    totalMonthlyExpense = totalExpense,
                    isLoading = false
                )
            }
        }
    }

    fun setEditingRecurring(recurring: RecurringTransaction?) {
        _uiState.value = _uiState.value.copy(editingRecurring = recurring)
    }

    fun toggleActive(recurring: RecurringTransaction) {
        viewModelScope.launch {
            financeRepository.setRecurringActive(recurring.id, !recurring.isActive)
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            financeRepository.deleteRecurringTransaction(recurring)
        }
    }

    fun updateRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            financeRepository.updateRecurringTransaction(recurring)
            _uiState.value = _uiState.value.copy(editingRecurring = null)
        }
    }
}
