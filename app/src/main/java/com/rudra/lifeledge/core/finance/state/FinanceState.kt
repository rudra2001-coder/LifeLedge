package com.rudra.lifeledge.core.finance.state

import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction

data class FinanceState(
    val accounts: List<Account> = emptyList(),
    val accountBalances: List<AccountBalance> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,

    val daily: DailySummary = DailySummary(
        date = "",
        income = 0.0,
        expense = 0.0,
        netBalance = 0.0,
        comparisonWithYesterday = 0.0,
        isPositiveTrend = true
    ),
    val weekly: WeeklySummary = WeeklySummary(
        startDate = "",
        endDate = "",
        totalExpense = 0.0,
        averageDailySpending = 0.0,
        comparisonWithLastWeek = 0.0,
        isPositiveTrend = true,
        days = emptyList()
    ),
    val monthly: MonthlySummary = MonthlySummary(
        income = 0.0,
        expense = 0.0,
        savings = 0.0,
        savingsRate = 0.0,
        comparisonWithLastMonth = 0.0,
        isPositiveTrend = true
    ),

    val alerts: List<FinanceAlert> = emptyList(),
    val activeAlert: FinanceAlert? = null,
    val insights: List<SpendingInsight> = emptyList(),
    val keyInsight: SpendingInsight? = null,
    val suggestions: List<SmartSuggestion> = emptyList(),
    val topSuggestion: SmartSuggestion? = null,

    val categorySpending: List<CategorySpending> = emptyList(),
    val behaviorPatterns: List<BehaviorPattern> = emptyList(),

    val healthScore: FinancialHealthScore = FinancialHealthScore(
        overallScore = 0,
        savingsScore = 0,
        spendingControlScore = 0,
        budgetAdherenceScore = 0,
        trendScore = 0,
        grade = "N/A",
        summary = ""
    ),

    val spendingPrediction: SpendingPrediction? = null,
    val runway: RunwayCalculation? = null,
    val behaviorModel: PersonalBehaviorModel? = null,
    val streakInfo: StreakInfo? = null,

    val status: DashboardStatus = DashboardStatus.HEALTHY,
    val statusMessage: String = "",

    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),

    val filterApplied: Boolean = false,
    val errorMessage: String? = null
)

enum class DashboardStatus {
    EXCELLENT,
    HEALTHY,
    MONITOR,
    WARNING,
    CRITICAL,
    NO_DATA
}

data class FinanceFilter(
    val startDate: String? = null,
    val endDate: String? = null,
    val transactionType: String? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val searchQuery: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    fun isActive(): Boolean = startDate != null || endDate != null || 
        transactionType != null || categoryId != null || accountId != null || 
        searchQuery != null || minAmount != null || maxAmount != null
}

sealed class FinanceAction {
    object Refresh : FinanceAction()
    object ClearError : FinanceAction()
    object DismissAlert : FinanceAction()
    data class ApplyFilter(val filter: FinanceFilter) : FinanceAction()
    object ClearFilter : FinanceAction()
    object UndoLastDelete : FinanceAction()
    data class SetBudget(val monthlyBudget: Double) : FinanceAction()
}

fun FinanceState.getTopPriorityAlert(): FinanceAlert? {
    return alerts.maxByOrNull { alert: FinanceAlert -> 
        when (alert.severity) {
            com.rudra.lifeledge.core.finance.model.Severity.INFO -> 1
            com.rudra.lifeledge.core.finance.model.Severity.WARNING -> 2
            com.rudra.lifeledge.core.finance.model.Severity.SUCCESS -> 3
        }
    }
}

fun FinanceState.calculateDashboardStatus(): DashboardStatus {
    return when {
        healthScore.overallScore >= 90 -> DashboardStatus.EXCELLENT
        healthScore.overallScore >= 70 -> DashboardStatus.HEALTHY
        runway?.urgency == com.rudra.lifeledge.core.finance.model.Urgency.CRITICAL -> DashboardStatus.CRITICAL
        runway?.urgency == com.rudra.lifeledge.core.finance.model.Urgency.WARNING -> DashboardStatus.WARNING
        runway?.urgency == com.rudra.lifeledge.core.finance.model.Urgency.MONITOR -> DashboardStatus.MONITOR
        monthly.savingsRate < 0 -> DashboardStatus.WARNING
        transactions.isEmpty() -> DashboardStatus.NO_DATA
        else -> DashboardStatus.HEALTHY
    }
}
