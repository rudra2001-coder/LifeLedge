package com.rudra.lifeledge.core.finance.intelligence

import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.sqrt

class AdaptiveIntelligenceEngine {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun predictWeeklySpending(transactions: List<Transaction>, days: Int = 7): PredictionResult {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }
        
        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        if (dailyExpenses.isEmpty()) {
            return PredictionResult(
                predictedAmount = 0.0,
                confidence = 0.0,
                basedOnDays = 0,
                modelUsed = PredictionModel.SIMPLE_AVERAGE,
                trend = Trend.STABLE,
                message = "Not enough data to predict"
            )
        }

        val recent7Days = last30Days.take(7).map { dailyExpenses[it] ?: 0.0 }
        val recent14Days = last30Days.take(14).map { dailyExpenses[it] ?: 0.0 }
        val recent30Days = last30Days.map { dailyExpenses[it] ?: 0.0 }

        val simpleAvg = recent7Days.average()
        val weightedAvg = recent7Days.mapIndexed { index, amount ->
            amount * ((index + 1).toDouble() / 7)
        }.sum() / 7

        val stdDev = calculateStdDev(recent30Days)
        val avg = recent30Days.average()

        val linearPrediction = if (recent14Days.isNotEmpty() && recent7Days.isNotEmpty()) {
            val firstHalf = recent14Days.take(7).average()
            val secondHalf = recent14Days.takeLast(7).average()
            val slope = (secondHalf - firstHalf) / 7
            simpleAvg + (slope * 7)
        } else simpleAvg

        val model: PredictionModel
        val prediction: Double
        val confidence: Double

        when {
            recent30Days.count { it > 0 } >= 20 -> {
                model = PredictionModel.LINEAR_TREND
                prediction = linearPrediction * 1.1
                confidence = 0.85
            }
            recent30Days.count { it > 0 } >= 14 -> {
                model = PredictionModel.WEIGHTED_RECENT
                prediction = weightedAvg * 7
                confidence = 0.75
            }
            else -> {
                model = PredictionModel.SIMPLE_AVERAGE
                prediction = simpleAvg * 7
                confidence = 0.5
            }
        }

        val trend = when {
            prediction > avg * 1.15 -> Trend.UP
            prediction < avg * 0.85 -> Trend.DOWN
            else -> Trend.STABLE
        }

        return PredictionResult(
            predictedAmount = prediction,
            confidence = confidence,
            basedOnDays = recent30Days.count { it > 0 },
            modelUsed = model,
            trend = trend,
            message = generatePredictionMessage(model, trend, prediction)
        )
    }

    private fun calculateStdDev(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val avg = values.average()
        val variance = values.map { (it - avg) * (it - avg) }.average()
        return sqrt(variance)
    }

    private fun generatePredictionMessage(model: PredictionModel, trend: Trend, amount: Double): String {
        val trendText = when (trend) {
            Trend.UP -> "increasing"
            Trend.DOWN -> "decreasing"
            Trend.STABLE -> "stable"
        }
        val modelText = when (model) {
            PredictionModel.LINEAR_TREND -> "linear regression"
            PredictionModel.WEIGHTED_RECENT -> "weighted average"
            PredictionModel.SIMPLE_AVERAGE -> "simple average"
        }
        return "Based on $modelText, your spending is $trendText. Predicted: ৳${String.format("%.0f", amount)}"
    }

    fun calculateRunway(
        currentBalance: Double,
        transactions: List<Transaction>,
        includeSavings: Boolean = true
    ): RunwayResult {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }

        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val avgDailyExpense = if (dailyExpenses.isNotEmpty()) {
            dailyExpenses.values.average()
        } else 0.0

        if (avgDailyExpense <= 0) {
            return RunwayResult(
                daysRemaining = Int.MAX_VALUE,
                projectedDate = null,
                urgency = Urgency.SAFE,
                message = "No significant expenses recorded. Your balance is safe.",
                dailyBurnRate = 0.0,
                confidence = 0.0
            )
        }

        val daysRemaining = (currentBalance / avgDailyExpense).toInt()
        val projectedDate = if (daysRemaining > 0 && daysRemaining < 3650) {
            today.plusDays(daysRemaining.toLong()).format(dateFormatter)
        } else null

        val urgency = when {
            daysRemaining > 60 -> Urgency.SAFE
            daysRemaining > 30 -> Urgency.MONITOR
            daysRemaining > 14 -> Urgency.WARNING
            else -> Urgency.CRITICAL
        }

        val stdDev = calculateStdDev(dailyExpenses.values.toList())
        val confidence = when {
            dailyExpenses.size >= 25 -> 0.9
            dailyExpenses.size >= 15 -> 0.75
            dailyExpenses.size >= 7 -> 0.6
            else -> 0.4
        }

        val message = when (urgency) {
            Urgency.SAFE -> "Your balance is healthy. You have approximately $daysRemaining days of spending remaining."
            Urgency.MONITOR -> "Monitor your spending. At current rate, you have ~$daysRemaining days before running low."
            Urgency.WARNING -> "Warning: Consider reducing expenses. You have about $daysRemaining days remaining."
            Urgency.CRITICAL -> "Critical action needed! Your balance may run out in $daysRemaining days."
        }

        return RunwayResult(
            daysRemaining = daysRemaining,
            projectedDate = projectedDate,
            urgency = urgency,
            message = message,
            dailyBurnRate = avgDailyExpense,
            confidence = confidence
        )
    }

    fun dynamicThreshold(transactions: List<Transaction>): ThresholdResult {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }

        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        if (dailyExpenses.isEmpty()) {
            return ThresholdResult(
                normalThreshold = 0.0,
                warningThreshold = 0.0,
                criticalThreshold = 0.0,
                methodUsed = "DEFAULT",
                standardDeviation = 0.0
            )
        }

        val avg = dailyExpenses.values.average()
        val stdDev = calculateStdDev(dailyExpenses.values.toList())

        val normalThreshold = avg + (stdDev * 0.5)
        val warningThreshold = avg + (stdDev * 1.2)
        val criticalThreshold = avg + (stdDev * 2.0)

        val method = if (dailyExpenses.size >= 20) "STATISTICAL" else "SIMPLE"

        return ThresholdResult(
            normalThreshold = normalThreshold,
            warningThreshold = warningThreshold,
            criticalThreshold = criticalThreshold,
            methodUsed = method,
            standardDeviation = stdDev
        )
    }

    fun calculateConfidenceScore(
        predictedBehavior: String,
        actualBehavior: Map<String, Int>
    ): ConfidenceResult {
        val totalObservations = actualBehavior.values.sum()
        if (totalObservations == 0) {
            return ConfidenceResult(
                confidence = 0.0,
                level = ConfidenceLevel.LOW,
                explanation = "Not enough data to calculate confidence"
            )
        }

        val matches = actualBehavior[predictedBehavior] ?: 0
        val confidence = (matches.toDouble() / totalObservations) * 100.0

        val level = when {
            confidence >= 80 -> ConfidenceLevel.HIGH
            confidence >= 60 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        return ConfidenceResult(
            confidence = confidence,
            level = level,
            explanation = "Based on $totalObservations observations, $predictedBehavior occurs $confidence% of the time"
        )
    }

    fun analyzeSpendingPatterns(transactions: List<Transaction>): BehaviorAnalysis {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }

        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }

        val dayOfWeekSpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { LocalDate.parse(it.date).dayOfWeek }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val avgByDay = dayOfWeekSpending.mapValues { it.value / dayOfWeekSpending.size }
        val highestDay = avgByDay.maxByOrNull { it.value }?.key
        val lowestDay = avgByDay.minByOrNull { it.value }?.key

        val overallAvg = if (dailyExpenses.isNotEmpty()) dailyExpenses.values.sumOf { list: List<Transaction> -> list.sumOf { it.amount } }.toDouble() / dailyExpenses.size else 0.0

        val volatility = if (dailyExpenses.isNotEmpty()) {
            val values: List<Double> = dailyExpenses.values.map { list: List<Transaction> -> list.sumOf { it.amount } }
            (calculateStdDev(values) / overallAvg) * 100.0
        } else 0.0

        val riskLevel = when {
            volatility > 50 -> RiskLevel.HIGH
            volatility > 25 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return BehaviorAnalysis(
            highestSpendingDay = highestDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            lowestSpendingDay = lowestDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            spendingVolatility = volatility,
            riskLevel = riskLevel,
            averageDailySpending = overallAvg,
            patternStrength = if (volatility < 20) "CONSISTENT" else "VARIABLE"
        )
    }

    fun generatePersonalizedInsights(transactions: List<Transaction>): List<PersonalizedInsight> {
        val insights = mutableListOf<PersonalizedInsight>()
        val analysis = analyzeSpendingPatterns(transactions)

        if (analysis.riskLevel == RiskLevel.HIGH) {
            insights.add(
                PersonalizedInsight(
                    type = InsightType.SPENDING_VOLATILITY,
                    title = "High Spending Variability",
                    message = "Your daily spending varies significantly. Consider setting a stricter daily budget.",
                    priority = Priority.HIGH
                )
            )
        }

        val dayPattern = analyzeDayOfWeekPattern(transactions)
        if (dayPattern != null) {
            insights.add(
                PersonalizedInsight(
                    type = InsightType.DAY_PATTERN,
                    title = "${dayPattern.day} Spending Pattern",
                    message = "You tend to spend more on ${dayPattern.day}s. Total: ৳${String.format("%.0f", dayPattern.amount)}",
                    priority = Priority.MEDIUM
                )
            )
        }

        val categoryAnalysis = analyzeTopCategories(transactions)
        if (categoryAnalysis != null && categoryAnalysis.percentage > 30) {
            insights.add(
                PersonalizedInsight(
                    type = InsightType.CATEGORY_CONCENTRATION,
                    title = "High ${categoryAnalysis.name} Spending",
                    message = "${categoryAnalysis.name} is ${String.format("%.0f", categoryAnalysis.percentage)}% of your expenses. Consider reducing by 10% to save ৳${String.format("%.0f", categoryAnalysis.amount * 0.1)}/month",
                    priority = Priority.HIGH,
                    potentialSavings = categoryAnalysis.amount * 0.1
                )
            )
        }

        return insights
    }

    private fun analyzeDayOfWeekPattern(transactions: List<Transaction>): DayPattern? {
        val daySpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { LocalDate.parse(it.date).dayOfWeek }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        if (daySpending.isEmpty()) return null

        val avgDaily = daySpending.values.average()
        val highDays = daySpending.filter { it.value > avgDaily * 1.3 }

        return highDays.maxByOrNull { it.value }?.let { (day, amount) ->
            DayPattern(day.name.lowercase().replaceFirstChar { it.uppercase() }, amount)
        }
    }

    private fun analyzeTopCategories(transactions: List<Transaction>): CategoryAnalysis? {
        val categorySpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        if (categorySpending.isEmpty()) return null

        val total = categorySpending.values.sum()
        val top = categorySpending.maxByOrNull { it.value } ?: return null

        return CategoryAnalysis(
            name = "Top Category",
            amount = top.value,
            percentage = (top.value / total) * 100
        )
    }
}

data class PredictionResult(
    val predictedAmount: Double,
    val confidence: Double,
    val basedOnDays: Int,
    val modelUsed: PredictionModel,
    val trend: Trend,
    val message: String
)

data class RunwayResult(
    val daysRemaining: Int,
    val projectedDate: String?,
    val urgency: Urgency,
    val message: String,
    val dailyBurnRate: Double,
    val confidence: Double
)

data class ThresholdResult(
    val normalThreshold: Double,
    val warningThreshold: Double,
    val criticalThreshold: Double,
    val methodUsed: String,
    val standardDeviation: Double
)

data class ConfidenceResult(
    val confidence: Double,
    val level: ConfidenceLevel,
    val explanation: String
)

data class BehaviorAnalysis(
    val highestSpendingDay: String,
    val lowestSpendingDay: String,
    val spendingVolatility: Double,
    val riskLevel: RiskLevel,
    val averageDailySpending: Double,
    val patternStrength: String
)

data class PersonalizedInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val priority: Priority,
    val potentialSavings: Double? = null
)

enum class ConfidenceLevel { HIGH, MEDIUM, LOW }
enum class RiskLevel { LOW, MEDIUM, HIGH }
enum class InsightType { SPENDING_VOLATILITY, DAY_PATTERN, CATEGORY_CONCENTRATION, TIME_PATTERN }
enum class Priority { LOW, MEDIUM, HIGH }

data class DayPattern(val day: String, val amount: Double)
data class CategoryAnalysis(val name: String, val amount: Double, val percentage: Double)
