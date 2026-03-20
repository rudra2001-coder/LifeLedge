package com.rudra.lifeledge.core.finance.intelligence

import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AdaptiveIntelligenceTest {

    @Test
    fun predictWeeklySpending_with_sufficient_data_uses_linear_trend() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = createTransactionsForDays(30, 1500.0)
        
        val result = engine.predictWeeklySpending(transactions)
        
        assertTrue(result.basedOnDays >= 20)
        assertTrue(result.confidence >= 0.75)
    }

    @Test
    fun predictWeeklySpending_with_limited_data_uses_simple_average() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = createTransactionsForDays(5, 1000.0)
        
        val result = engine.predictWeeklySpending(transactions)
        
        assertEquals(PredictionModel.SIMPLE_AVERAGE, result.modelUsed)
    }

    @Test
    fun calculateRunway_with_high_balance_returns_safe() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = createTransactionsForDays(30, 1000.0)
        
        val result = engine.calculateRunway(100000.0, transactions)
        
        assertEquals(Urgency.SAFE, result.urgency)
        assertTrue(result.daysRemaining > 30)
    }

    @Test
    fun calculateRunway_with_low_balance_returns_critical() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = createTransactionsForDays(30, 5000.0)
        
        val result = engine.calculateRunway(10000.0, transactions)
        
        assertTrue(result.daysRemaining <= 10)
    }

    @Test
    fun calculateRunway_with_zero_expenses_returns_safe() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = emptyList<Transaction>()
        
        val result = engine.calculateRunway(50000.0, transactions)
        
        assertEquals(Urgency.SAFE, result.urgency)
        assertEquals(Int.MAX_VALUE, result.daysRemaining)
    }

    @Test
    fun dynamicThreshold_calculates_correct_thresholds() {
        val engine = AdaptiveIntelligenceEngine()
        val transactions = createTransactionsForDays(30, 2000.0)
        
        val result = engine.dynamicThreshold(transactions)
        
        assertTrue(result.normalThreshold > 0)
        assertTrue(result.warningThreshold > result.normalThreshold)
        assertTrue(result.criticalThreshold > result.warningThreshold)
    }

    @Test
    fun calculateConfidenceScore_with_matching_predictions_returns_high() {
        val engine = AdaptiveIntelligenceEngine()
        
        val result = engine.calculateConfidenceScore(
            predictedBehavior = "Friday",
            mapOf("Friday" to 8, "Saturday" to 2)
        )
        
        assertTrue(result.confidence >= 80)
        assertEquals(ConfidenceLevel.HIGH, result.level)
    }

    @Test
    fun analyzeSpendingPatterns_with_varied_spending_returns_high_volatility() {
        val engine = AdaptiveIntelligenceEngine()
        
        val transactions = mutableListOf<Transaction>()
        val amounts = listOf(500.0, 5000.0, 800.0, 4500.0, 1000.0)
        
        for (i in 0 until 5) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = amounts[i],
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        
        val result = engine.analyzeSpendingPatterns(transactions)
        
        assertTrue(result.spendingVolatility > 50)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }

    @Test
    fun generatePersonalizedInsights_with_high_category_spending_returns_concentration_insight() {
        val engine = AdaptiveIntelligenceEngine()
        
        val transactions = mutableListOf<Transaction>()
        
        for (i in 0 until 20) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = 5000.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        for (i in 20 until 30) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = 100.0,
                    type = TransactionType.EXPENSE,
                    categoryId = 2,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        
        val insights = engine.generatePersonalizedInsights(transactions)
        
        assertTrue(insights.any { it.type == InsightType.CATEGORY_CONCENTRATION })
    }

    @Test
    fun predictWeeklySpending_with_increasing_trend_returns_up() {
        val engine = AdaptiveIntelligenceEngine()
        
        val transactions = mutableListOf<Transaction>()
        
        for (i in 0 until 14) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = 1000.0 + i * 100,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        
        val result = engine.predictWeeklySpending(transactions)
        
        assertEquals(Trend.UP, result.trend)
    }

    @Test
    fun predictWeeklySpending_with_decreasing_trend_returns_down() {
        val engine = AdaptiveIntelligenceEngine()
        
        val transactions = mutableListOf<Transaction>()
        
        for (i in 0 until 14) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = 3000.0 - i * 150,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        
        val result = engine.predictWeeklySpending(transactions)
        
        assertEquals(Trend.DOWN, result.trend)
    }

    @Test
    fun confidence_score_with_no_data_returns_zero() {
        val engine = AdaptiveIntelligenceEngine()
        
        val result = engine.calculateConfidenceScore("Friday", emptyMap())
        
        assertEquals(0.0, result.confidence, 0.01)
        assertEquals(ConfidenceLevel.LOW, result.level)
    }

    private fun createTransactionsForDays(days: Int, avgAmount: Double): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        for (i in 0 until days) {
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    date = LocalDate.now().minusDays(i.toLong()).toString(),
                    amount = avgAmount,
                    type = TransactionType.EXPENSE,
                    categoryId = 1,
                    accountId = 1,
                    toAccountId = null,
                    payee = null,
                    notes = null,
                    isRecurring = false,
                    recurringId = null,
                    cardId = null,
                    isCleared = true,
                    attachment = null,
                    location = null,
                    tags = ""
                )
            )
        }
        return transactions
    }
}
