package com.rudra.lifeledge.core.finance.model

import com.rudra.lifeledge.data.local.entity.TransactionType

data class MonthlySummary(
    val income: Double,
    val expense: Double,
    val savings: Double,
    val savingsRate: Double,
    val comparisonWithLastMonth: Double,
    val isPositiveTrend: Boolean
)

data class DailySummary(
    val date: String,
    val income: Double,
    val expense: Double,
    val netBalance: Double,
    val comparisonWithYesterday: Double,
    val isPositiveTrend: Boolean
)

data class WeeklySummary(
    val startDate: String,
    val endDate: String,
    val totalExpense: Double,
    val averageDailySpending: Double,
    val comparisonWithLastWeek: Double,
    val isPositiveTrend: Boolean,
    val days: List<DailySummary>
)

data class SpendingInsight(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val trend: Trend,
    val message: String
)

enum class Trend { UP, DOWN, STABLE }

enum class AlertType { OVERSPENDING, LOW_SPENDING, BUDGET_WARNING, STREAK_POSITIVE, STreak_NEGATIVE }

enum class Severity { INFO, WARNING, SUCCESS }

data class FinanceAlert(
    val id: Long = System.currentTimeMillis(),
    val type: AlertType,
    val title: String = "",
    val message: String,
    val severity: Severity,
    val timestamp: String
)

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val icon: String,
    val color: Int
)

data class AccountBalance(
    val accountId: Long,
    val accountName: String,
    val openingBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val currentBalance: Double,
    val transactionCount: Int
)

data class BehaviorPattern(
    val dayOfWeek: String,
    val averageSpending: Double,
    val isHighestSpendingDay: Boolean
)

data class BudgetStatus(
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentUsed: Double,
    val daysRemainingInMonth: Int,
    val projectedOverspend: Double?
)
