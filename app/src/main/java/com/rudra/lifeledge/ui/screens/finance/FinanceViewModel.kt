package com.rudra.lifeledge.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FinanceUiState(
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val recentlyDeletedTransaction: Transaction? = null
)

class FinanceViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val today = LocalDate.now()
            val monthStart = today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val monthEnd = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

            launch {
                financeRepository.getTotalBalance().collect { balance ->
                    _uiState.value = _uiState.value.copy(totalBalance = balance ?: 0.0)
                }
            }

            launch {
                financeRepository.getTransactionsBetween(monthStart, monthEnd).collect { transactions ->
                    _uiState.value = _uiState.value.copy(transactions = transactions)
                }
            }

            launch {
                financeRepository.getMonthlyIncome(monthStart, monthEnd).collect { income ->
                    _uiState.value = _uiState.value.copy(monthlyIncome = income)
                }
            }

            launch {
                financeRepository.getMonthlyExpense(monthStart, monthEnd).collect { expense ->
                    _uiState.value = _uiState.value.copy(monthlyExpense = expense, isLoading = false)
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = transaction)
            financeRepository.deleteTransaction(transaction)
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedTransaction?.let { transaction ->
                financeRepository.saveTransaction(transaction)
                _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = null)
            }
        }
    }

    fun clearDeletedTransaction() {
        _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = null)
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            // Add account deletion logic if needed
        }
    }
}
