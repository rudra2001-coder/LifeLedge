package com.rudra.lifeledge.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FinanceUiState(
    val accounts: List<Account> = emptyList(),
    val accountBalances: List<AccountBalance> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthlySummary: MonthlySummary? = null,
    val dailySummary: DailySummary? = null,
    val weeklySummary: WeeklySummary? = null,
    val spendingInsights: List<SpendingInsight> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val alerts: List<FinanceAlert> = emptyList(),
    val behaviorPatterns: List<BehaviorPattern> = emptyList(),
    val budgetStatus: BudgetStatus? = null,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val recentlyDeletedTransaction: Transaction? = null,
    val filterStartDate: String? = null,
    val filterEndDate: String? = null,
    val filterType: TransactionType? = null,
    val filterCategoryId: Long? = null,
    val filterAccountId: Long? = null,
    val searchQuery: String = "",
    val spendingPrediction: SpendingPrediction? = null,
    val runwayCalculation: RunwayCalculation? = null,
    val smartSuggestions: List<SmartSuggestion> = emptyList(),
    val behaviorModel: PersonalBehaviorModel? = null,
    val healthScore: FinancialHealthScore? = null,
    val streakInfo: StreakInfo? = null
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

            launch {
                financeRepository.getNetBalance().collect { balance ->
                    _uiState.value = _uiState.value.copy(totalBalance = balance)
                }
            }

            launch {
                financeRepository.getMonthlySummary().collect { summary ->
                    _uiState.value = _uiState.value.copy(monthlySummary = summary)
                }
            }

            launch {
                financeRepository.getDailySummary().collect { summary ->
                    _uiState.value = _uiState.value.copy(dailySummary = summary)
                }
            }

            launch {
                financeRepository.getWeeklySummary().collect { summary ->
                    _uiState.value = _uiState.value.copy(weeklySummary = summary)
                }
            }

            launch {
                financeRepository.getAlerts().collect { alerts ->
                    _uiState.value = _uiState.value.copy(alerts = alerts)
                }
            }

            launch {
                financeRepository.getSpendingInsights().collect { insights ->
                    _uiState.value = _uiState.value.copy(spendingInsights = insights)
                }
            }

            launch {
                financeRepository.getCategorySpending().collect { spending ->
                    _uiState.value = _uiState.value.copy(categorySpending = spending)
                }
            }

            launch {
                financeRepository.getAccountBalances().collect { balances ->
                    _uiState.value = _uiState.value.copy(accountBalances = balances)
                }
            }

            launch {
                financeRepository.getBehaviorPatterns().collect { patterns ->
                    _uiState.value = _uiState.value.copy(behaviorPatterns = patterns)
                }
            }

            launch {
                financeRepository.getSpendingPrediction().collect { prediction ->
                    _uiState.value = _uiState.value.copy(spendingPrediction = prediction)
                }
            }

            launch {
                financeRepository.getRunwayCalculation().collect { runway ->
                    _uiState.value = _uiState.value.copy(runwayCalculation = runway)
                }
            }

            launch {
                financeRepository.getSmartSuggestions().collect { suggestions ->
                    _uiState.value = _uiState.value.copy(smartSuggestions = suggestions)
                }
            }

            launch {
                financeRepository.getFinancialHealthScore().collect { score ->
                    _uiState.value = _uiState.value.copy(healthScore = score)
                }
            }

            launch {
                financeRepository.getStreakInfo().collect { streak ->
                    _uiState.value = _uiState.value.copy(streakInfo = streak)
                }
            }

            launch {
                financeRepository.getPersonalBehaviorModel().collect { model ->
                    _uiState.value = _uiState.value.copy(behaviorModel = model)
                }
            }

            launch {
                financeRepository.getAllTransactions().collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadBudgetStatus(monthlyBudget: Double) {
        viewModelScope.launch {
            financeRepository.getBudgetStatus(monthlyBudget).collect { status ->
                _uiState.value = _uiState.value.copy(budgetStatus = status)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun applyFilters(
        startDate: String? = _uiState.value.filterStartDate,
        endDate: String? = _uiState.value.filterEndDate,
        type: TransactionType? = _uiState.value.filterType,
        categoryId: Long? = _uiState.value.filterCategoryId,
        accountId: Long? = _uiState.value.filterAccountId,
        searchQuery: String = _uiState.value.searchQuery
    ) {
        _uiState.value = _uiState.value.copy(
            filterStartDate = startDate,
            filterEndDate = endDate,
            filterType = type,
            filterCategoryId = categoryId,
            filterAccountId = accountId,
            searchQuery = searchQuery
        )

        viewModelScope.launch {
            financeRepository.getFilteredTransactions(
                startDate, endDate, type, categoryId, accountId, searchQuery.ifBlank { null }
            ).collect { transactions ->
                _uiState.value = _uiState.value.copy(filteredTransactions = transactions)
            }
        }
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filterStartDate = null,
            filterEndDate = null,
            filterType = null,
            filterCategoryId = null,
            filterAccountId = null,
            searchQuery = "",
            filteredTransactions = emptyList()
        )
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = transaction)
            financeRepository.deleteTransactionWithBalanceUpdate(transaction)
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedTransaction?.let { transaction ->
                financeRepository.addTransactionWithBalanceUpdate(transaction)
                _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = null)
            }
        }
    }

    fun clearDeletedTransaction() {
        _uiState.value = _uiState.value.copy(recentlyDeletedTransaction = null)
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeRepository.addTransactionWithBalanceUpdate(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            financeRepository.financeEngine.updateTransactionWithBalanceUpdate(transaction)
        }
    }
}
