package com.rudra.lifeledge.ui.screens.income

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.Frequency
import com.rudra.lifeledge.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class IncomeUiState(
    val amount: String = "",
    val selectedSource: IncomeSource? = null,
    val description: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val recurringDay: Int = LocalDate.now().dayOfMonth,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

enum class IncomeSource(val title: String, val icon: ImageVector, val color: Color) {
    SALARY("Salary", Icons.Default.Work, Color(0xFF22C55E)),
    FREELANCE("Freelance", Icons.Default.LaptopMac, Color(0xFF3B82F6)),
    INVESTMENT("Investment", Icons.Default.TrendingUp, Color(0xFF8B5CF6)),
    BUSINESS("Business", Icons.Default.Store, Color(0xFFF59E0B)),
    GIFT("Gift", Icons.Default.CardGiftcard, Color(0xFFEC4899)),
    RENTAL("Rental", Icons.Default.Home, Color(0xFF06B6D4)),
    OTHER("Other", Icons.Default.MoreHoriz, Color(0xFF6B7280))
}

enum class RecurringFrequency(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

class IncomeViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateSource(source: IncomeSource) {
        _uiState.value = _uiState.value.copy(selectedSource = source)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun toggleRecurring() {
        _uiState.value = _uiState.value.copy(isRecurring = !_uiState.value.isRecurring)
    }

    fun updateFrequency(frequency: RecurringFrequency) {
        _uiState.value = _uiState.value.copy(recurringFrequency = frequency)
    }

    fun updateRecurringDay(day: Int) {
        _uiState.value = _uiState.value.copy(recurringDay = day)
    }

    fun saveIncome() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val amountValue = _uiState.value.amount.toDoubleOrNull() ?: 0.0
                if (amountValue > 0) {
                    val state = _uiState.value
                    val today = LocalDate.now()
                    
                    val transaction = Transaction(
                        date = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        amount = amountValue,
                        type = TransactionType.INCOME,
                        categoryId = 0L,
                        accountId = 1L,
                        toAccountId = null,
                        payee = null,
                        notes = state.description,
                        isRecurring = state.isRecurring,
                        recurringId = null,
                        cardId = null,
                        isCleared = true,
                        attachment = null,
                        location = null,
                        tags = state.selectedSource?.name ?: ""
                    )
                    val transactionId = financeRepository.saveTransaction(transaction)

                    if (state.isRecurring) {
                        val frequency = when (state.recurringFrequency) {
                            RecurringFrequency.DAILY -> Frequency.DAILY
                            RecurringFrequency.WEEKLY -> Frequency.WEEKLY
                            RecurringFrequency.MONTHLY -> Frequency.MONTHLY
                            RecurringFrequency.YEARLY -> Frequency.YEARLY
                        }
                        
                        val nextDate = calculateNextDate(
                            state.selectedDate,
                            frequency,
                            1,
                            state.recurringDay
                        )

                        val recurringTransaction = RecurringTransaction(
                            name = state.selectedSource?.title ?: "Income",
                            amount = amountValue,
                            type = TransactionType.INCOME,
                            categoryId = 0L,
                            accountId = 1L,
                            frequency = frequency,
                            interval = 1,
                            executeDay = if (frequency == Frequency.MONTHLY) state.recurringDay else null,
                            startDate = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate = null,
                            nextDate = nextDate,
                            lastExecutedDate = null,
                            payee = null,
                            notes = state.description,
                            isActive = true
                        )
                        val recurringId = financeRepository.saveRecurringTransaction(recurringTransaction)
                        
                        val updatedTransaction = transaction.copy(
                            id = transactionId,
                            recurringId = recurringId
                        )
                        financeRepository.saveTransaction(updatedTransaction)
                    }

                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateNextDate(
        currentDate: LocalDate,
        frequency: Frequency,
        interval: Int,
        executeDay: Int
    ): String {
        return when (frequency) {
            Frequency.DAILY -> currentDate.plusDays(interval.toLong())
            Frequency.WEEKLY -> currentDate.plusWeeks(interval.toLong())
            Frequency.MONTHLY -> {
                val nextMonth = currentDate.plusMonths(interval.toLong())
                val dayOfMonth = minOf(executeDay, nextMonth.lengthOfMonth())
                nextMonth.withDayOfMonth(dayOfMonth)
            }
            Frequency.YEARLY -> currentDate.plusYears(interval.toLong())
        }.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
