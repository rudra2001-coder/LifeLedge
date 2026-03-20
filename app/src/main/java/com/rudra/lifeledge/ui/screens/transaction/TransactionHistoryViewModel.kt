package com.rudra.lifeledge.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.core.finance.usecase.DeleteTransactionUseCase
import com.rudra.lifeledge.core.finance.usecase.GetCashFlowUseCase
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TransactionFilters(
    val startDate: String? = null,
    val endDate: String? = null,
    val accountId: Long? = null,
    val type: TransactionType? = null,
    val searchQuery: String? = null
)

data class TransactionHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filters: TransactionFilters = TransactionFilters(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val hasMorePages: Boolean = false
)

class TransactionHistoryViewModel(
    private val transactionDao: TransactionDao,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val cashFlowUseCase: GetCashFlowUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val pageSize = 50
    private var currentOffset = 0
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            currentOffset = 0
            
            try {
                val transactions = transactionDao.getRecentTransactions(100).first()
                
                // Apply filters
                val filtered = applyFilters(transactions, _uiState.value.filters)
                
                // Calculate totals
                val totalIncome = filtered
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                
                val totalExpense = filtered
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    filteredTransactions = filtered,
                    isLoading = false,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    hasMorePages = transactions.size >= pageSize
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load transactions: ${e.message}"
                )
            }
        }
    }
    
    fun loadMore() {
        if (!_uiState.value.hasMorePages || _uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            currentOffset += pageSize
            
            try {
                val moreTransactions = transactionDao.getRecentTransactions(100).first()
                val allTransactions = _uiState.value.transactions + moreTransactions
                val filtered = applyFilters(allTransactions, _uiState.value.filters)
                
                _uiState.value = _uiState.value.copy(
                    transactions = allTransactions,
                    filteredTransactions = filtered,
                    isLoading = false,
                    hasMorePages = moreTransactions.size >= pageSize
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load more: ${e.message}"
                )
            }
        }
    }
    
    fun setDateRange(startDate: String?, endDate: String?) {
        val newFilters = _uiState.value.filters.copy(startDate = startDate, endDate = endDate)
        _uiState.value = _uiState.value.copy(filters = newFilters)
        applyFiltersAndReload()
    }
    
    fun setAccountFilter(accountId: Long?) {
        val newFilters = _uiState.value.filters.copy(accountId = accountId)
        _uiState.value = _uiState.value.copy(filters = newFilters)
        applyFiltersAndReload()
    }
    
    fun setTypeFilter(type: TransactionType?) {
        val newFilters = _uiState.value.filters.copy(type = type)
        _uiState.value = _uiState.value.copy(filters = newFilters)
        applyFiltersAndReload()
    }
    
    fun setSearchQuery(query: String?) {
        val newFilters = _uiState.value.filters.copy(searchQuery = query)
        _uiState.value = _uiState.value.copy(filters = newFilters)
        applyFiltersAndReload()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(filters = TransactionFilters())
        applyFiltersAndReload()
    }
    
    private fun applyFiltersAndReload() {
        val filtered = applyFilters(_uiState.value.transactions, _uiState.value.filters)
        
        val totalIncome = filtered
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val totalExpense = filtered
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        _uiState.value = _uiState.value.copy(
            filteredTransactions = filtered,
            totalIncome = totalIncome,
            totalExpense = totalExpense
        )
    }
    
    private fun applyFilters(transactions: List<Transaction>, filters: TransactionFilters): List<Transaction> {
        return transactions.filter { transaction ->
            var matches = true
            
            // Date range filter
            if (filters.startDate != null && transaction.date < filters.startDate) {
                matches = false
            }
            if (filters.endDate != null && transaction.date > filters.endDate) {
                matches = false
            }
            
            // Account filter
            if (filters.accountId != null && 
                transaction.accountId != filters.accountId && 
                transaction.toAccountId != filters.accountId) {
                matches = false
            }
            
            // Type filter
            if (filters.type != null && transaction.type != filters.type) {
                matches = false
            }
            
            // Search query
            if (filters.searchQuery != null && filters.searchQuery.isNotBlank()) {
                val query = filters.searchQuery.lowercase()
                val matchesSearch = 
                    transaction.notes?.lowercase()?.contains(query) == true ||
                    transaction.payee?.lowercase()?.contains(query) == true ||
                    transaction.tags.lowercase().contains(query)
                if (!matchesSearch) matches = false
            }
            
            matches
        }
    }
    
    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = deleteTransactionUseCase(transactionId)
            
            when (result) {
                is com.rudra.lifeledge.core.finance.engine.TransactionResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadTransactions() // Reload to get updated data
                }
                is com.rudra.lifeledge.core.finance.engine.TransactionResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun refresh() {
        loadTransactions()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
