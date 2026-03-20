package com.rudra.lifeledge.ui.screens.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.core.finance.usecase.EmiCalculator
import com.rudra.lifeledge.core.finance.usecase.PayEmiUseCase
import com.rudra.lifeledge.data.local.dao.LoanDao
import com.rudra.lifeledge.data.local.entity.Loan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoanUiState(
    val loans: List<Loan> = emptyList(),
    val selectedLoan: Loan? = null,
    val calculatedEmi: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class LoanCreateState(
    val name: String = "",
    val type: String = "PERSONAL",
    val principal: String = "",
    val interestRate: String = "",
    val tenureMonths: String = "",
    val lender: String = ""
)

class LoanViewModel(
    private val loanDao: LoanDao,
    private val payEmiUseCase: PayEmiUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()
    
    private val _createState = MutableStateFlow(LoanCreateState())
    val createState: StateFlow<LoanCreateState> = _createState.asStateFlow()
    
    init {
        loadLoans()
    }
    
    private fun loadLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loanDao.getAllLoans().collect { loans ->
                _uiState.value = _uiState.value.copy(
                    loans = loans,
                    isLoading = false
                )
            }
        }
    }
    
    fun selectLoan(loan: Loan) {
        _uiState.value = _uiState.value.copy(selectedLoan = loan)
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedLoan = null)
    }
    
    fun calculateEmi() {
        val state = _createState.value
        val principal = state.principal.toDoubleOrNull() ?: return
        val rate = state.interestRate.toDoubleOrNull() ?: return
        val tenure = state.tenureMonths.toIntOrNull() ?: return
        
        val emi = EmiCalculator.calculateEmi(principal, rate, tenure)
        _uiState.value = _uiState.value.copy(calculatedEmi = emi)
    }
    
    fun updateCreateName(name: String) {
        _createState.value = _createState.value.copy(name = name)
    }
    
    fun updateCreateType(type: String) {
        _createState.value = _createState.value.copy(type = type)
    }
    
    fun updateCreatePrincipal(principal: String) {
        _createState.value = _createState.value.copy(principal = principal)
        calculateEmi()
    }
    
    fun updateCreateInterestRate(rate: String) {
        _createState.value = _createState.value.copy(interestRate = rate)
        calculateEmi()
    }
    
    fun updateCreateTenure(tenure: String) {
        _createState.value = _createState.value.copy(tenureMonths = tenure)
        calculateEmi()
    }
    
    fun updateCreateLender(lender: String) {
        _createState.value = _createState.value.copy(lender = lender)
    }
    
    fun createLoan() {
        viewModelScope.launch {
            val state = _createState.value
            
            val principal = state.principal.toDoubleOrNull()
            val rate = state.interestRate.toDoubleOrNull()
            val tenure = state.tenureMonths.toIntOrNull()
            
            if (principal == null || rate == null || tenure == null) {
                _uiState.value = _uiState.value.copy(error = "Invalid loan details")
                return@launch
            }
            
            val emi = EmiCalculator.calculateEmi(principal, rate, tenure)
            val startDate = java.time.LocalDate.now()
            val endDate = startDate.plusMonths(tenure.toLong())
            
            val loan = Loan(
                name = state.name,
                type = com.rudra.lifeledge.data.local.entity.LoanType.valueOf(state.type),
                totalAmount = principal,
                remainingAmount = principal,
                interestRate = rate,
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                lender = state.lender,
                monthlyEMI = emi,
                notes = null
            )
            
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                loanDao.insertLoan(loan)
                _createState.value = LoanCreateState()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Loan created successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create loan: ${e.message}"
                )
            }
        }
    }
    
    fun payEmi(loanId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = payEmiUseCase(
                loanId = loanId,
                paymentAmount = null, // Use the loan's EMI
                fromAccountId = null // Use default account
            )
            
            when (result) {
                is com.rudra.lifeledge.core.finance.engine.TransactionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "EMI paid successfully"
                    )
                    // Refresh loans
                    loadLoans()
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
    
    fun payCustomAmount(loanId: Long, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = payEmiUseCase(
                loanId = loanId,
                paymentAmount = amount,
                fromAccountId = null
            )
            
            when (result) {
                is com.rudra.lifeledge.core.finance.engine.TransactionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Payment successful"
                    )
                    loadLoans()
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
    
    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            try {
                loanDao.deleteLoan(loan)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Loan deleted"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete loan: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
