package com.rudra.lifeledge.core.finance.model

data class SpendingPrediction(
    val predictedWeeklyExpense: Double,
    val confidence: Double,
    val basedOnDays: Int,
    val dailyAverage: Double,
    val predictionModel: PredictionModel,
    val weeklyTrend: Trend,
    val message: String
)

enum class PredictionModel { SIMPLE_AVERAGE, WEIGHTED_RECENT, LINEAR_TREND }

data class RunwayCalculation(
    val currentBalance: Double,
    val averageDailyExpense: Double,
    val daysRemaining: Int,
    val projectedZeroDate: String?,
    val urgency: Urgency,
    val message: String
)

enum class Urgency { SAFE, MONITOR, WARNING, CRITICAL }

data class SmartSuggestion(
    val id: Long = System.currentTimeMillis(),
    val type: SuggestionType,
    val title: String,
    val description: String,
    val potentialSavings: Double?,
    val actionRequired: Boolean,
    val priority: Int,
    val icon: String
)

enum class SuggestionType { REDUCE_SPENDING, INCREASE_INCOME, BUDGET_ADJUSTMENT, SAVING_OPPORTUNITY, OVERSPENDING_WARNING }

data class PersonalBehaviorModel(
    val daySpendingRank: Map<Int, Double>,
    val hourSpendingRank: Map<Int, Double>,
    val categoryPreferences: Map<Long, Double>,
    val overspendingTriggers: List<OverspendingTrigger>,
    val averageTransactionSize: Double,
    val savingScore: Int
)

data class OverspendingTrigger(
    val triggerType: String,
    val description: String,
    val frequency: Int,
    val averageOverspend: Double
)

data class IntegrityCheck(
    val isValid: Boolean,
    val accountMismatches: List<AccountMismatch>,
    val transactionCount: Long,
    val lastVerified: String
)

data class AccountMismatch(
    val accountId: Long,
    val accountName: String,
    val calculatedBalance: Double,
    val storedBalance: Double,
    val difference: Double
)

data class StreakInfo(
    val underBudgetDays: Int,
    val spendingControlDays: Int,
    val savingsStreakDays: Int,
    val longestUnderBudget: Int,
    val lastUpdated: String
)

data class FinancialHealthScore(
    val overallScore: Int,
    val savingsScore: Int,
    val spendingControlScore: Int,
    val budgetAdherenceScore: Int,
    val trendScore: Int,
    val grade: String,
    val summary: String
)
