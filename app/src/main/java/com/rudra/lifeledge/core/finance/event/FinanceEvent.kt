package com.rudra.lifeledge.core.finance.event

import com.rudra.lifeledge.data.local.entity.Transaction

sealed class FinanceEvent {
    abstract val timestamp: Long

    data class AddTransaction(
        val transaction: Transaction,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class DeleteTransaction(
        val transaction: Transaction,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class UpdateTransaction(
        val oldTransaction: Transaction,
        val newTransaction: Transaction,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class TransferBetweenAccounts(
        val fromAccountId: Long,
        val toAccountId: Long,
        val amount: Double,
        val note: String?,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    object RefreshDashboard : FinanceEvent() {
        override val timestamp: Long = System.currentTimeMillis()
    }

    object RecalculateAllSummaries : FinanceEvent() {
        override val timestamp: Long = System.currentTimeMillis()
    }

    data class BudgetExceeded(
        val categoryId: Long,
        val categoryName: String,
        val currentSpending: Double,
        val budgetLimit: Double,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class LowBalanceWarning(
        val accountId: Long,
        val accountName: String,
        val currentBalance: Double,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class OverspendingDetected(
        val currentSpending: Double,
        val averageSpending: Double,
        val threshold: Double,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    data class StreakMilestone(
        val streakType: String,
        val currentStreak: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : FinanceEvent()

    object ValidateIntegrity : FinanceEvent() {
        override val timestamp: Long = System.currentTimeMillis()
    }

    object FixIntegrityIssues : FinanceEvent() {
        override val timestamp: Long = System.currentTimeMillis()
    }
}

sealed class FinanceResult {
    data class TransactionAdded(val transactionId: Long) : FinanceResult()
    data class TransactionDeleted(val transactionId: Long) : FinanceResult()
    data class TransactionUpdated(val transactionId: Long) : FinanceResult()
    data class IntegrityCheckResult(val isValid: Boolean, val issues: List<String>) : FinanceResult()
    data class Error(val message: String, val exception: Exception? = null) : FinanceResult()
    object Success : FinanceResult()
    data class AlertTriggered(val alert: FinanceAlert) : FinanceResult()
}

data class FinanceAlert(
    val id: Long = System.currentTimeMillis(),
    val type: AlertType,
    val title: String,
    val message: String,
    val priority: AlertPriority = AlertPriority.MEDIUM,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)

enum class AlertType {
    OVERSPENDING,
    LOW_BALANCE,
    BUDGET_EXCEEDED,
    STREAK_MILESTONE,
    SAVINGS_GOAL_REACHED,
    UNUSUAL_SPENDING,
    SYSTEM_ERROR
}

enum class AlertPriority { LOW, MEDIUM, HIGH, CRITICAL }
