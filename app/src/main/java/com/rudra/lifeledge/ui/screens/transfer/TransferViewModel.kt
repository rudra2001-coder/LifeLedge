package com.rudra.lifeledge.ui.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransferUiState(
    val savingsBalance: Double = 0.0,
    val transferAmount: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class TransferViewModel(private val financeRepository: FinanceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        loadSavingsBalance()
    }

    private fun loadSavingsBalance() {
        viewModelScope.launch {
            financeRepository.getSavingsBalance().collect { balance ->
                _uiState.value = _uiState.value.copy(savingsBalance = balance)
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            transferAmount = amount,
            errorMessage = null
        )
    }

    fun transfer() {
        val amount = _uiState.value.transferAmount.toDoubleOrNull() ?: 0.0
        
        if (amount > _uiState.value.savingsBalance) {
            _uiState.value = _uiState.value.copy(errorMessage = "Amount exceeds available savings")
            return
        }
        
        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                financeRepository.transferFromSavings(amount, "Transfer from savings")
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Transfer failed. Please try again."
                )
            }
        }
    }
}
