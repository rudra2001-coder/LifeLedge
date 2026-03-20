package com.rudra.lifeledge.core.finance.identity

import com.rudra.lifeledge.core.finance.model.FinancialHealthScore
import com.rudra.lifeledge.core.finance.model.SpendingInsight
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class FinancialIdentityEngine {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun analyzeIdentity(transactions: List<Transaction>): FinancialIdentity {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }
        
        val monthlyTransactions = transactions.filter { it.date in last30Days }
        
        val income = monthlyTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savings = income - expense
        
        val disciplineScore = calculateDisciplineScore(savings, income, monthlyTransactions)
        val riskScore = calculateRiskScore(monthlyTransactions)
        val consistencyScore = calculateConsistencyScore(monthlyTransactions)
        
        val userType = determineUserType(disciplineScore, riskScore, savings, income)
        
        return FinancialIdentity(
            type = userType,
            disciplineScore = disciplineScore,
            riskScore = riskScore,
            consistencyScore = consistencyScore,
            savingsRate = if (income > 0) (savings / income * 100).toFloat() else 0f,
            averageSpending = if (monthlyTransactions.isNotEmpty()) monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.map { it.amount }.average().toFloat() else 0f,
            totalTransactions = monthlyTransactions.size,
            analyzedAt = today.format(dateFormatter)
        )
    }

    private fun calculateDisciplineScore(savings: Double, income: Double, transactions: List<Transaction>): Float {
        if (income <= 0) return 50f
        
        val savingsRate = (savings / income * 100)
        
        var onTimeCount = 0
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }
        
        val dailyExpenses = totalExpenses.groupBy { it.date }
        val avgDaily = if (dailyExpenses.isNotEmpty()) dailyExpenses.values.sumOf { it.sumOf { t -> t.amount } } / dailyExpenses.size else 0.0
        
        for ((_, dayTxns) in dailyExpenses) {
            val dayTotal = dayTxns.sumOf { it.amount }
            if (dayTotal <= avgDaily * 1.2) onTimeCount++
        }
        
        val consistencyRatio = if (dailyExpenses.isNotEmpty()) onTimeCount.toFloat() / dailyExpenses.size else 0f
        
        val savingsScore = when {
            savingsRate >= 20 -> 100f
            savingsRate >= 15 -> 80f
            savingsRate >= 10 -> 60f
            savingsRate >= 5 -> 40f
            savingsRate > 0 -> 20f
            else -> 0f
        }
        
        return ((savingsScore * 0.6f) + (consistencyRatio * 100 * 0.4f)).coerceIn(0f, 100f)
    }

    private fun calculateRiskScore(transactions: List<Transaction>): Float {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }
        
        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        
        if (dailyExpenses.isEmpty()) return 50f
        
        val avg = dailyExpenses.values.average()
        val variance = dailyExpenses.values.map { abs(it - avg) }.average()
        val cv = if (avg > 0) (variance / avg * 100) else 0.0
        
        return when {
            cv < 20 -> 20f
            cv < 40 -> 40f
            cv < 60 -> 60f
            cv < 80 -> 80f
            else -> 100f
        }
    }

    private fun calculateConsistencyScore(transactions: List<Transaction>): Float {
        val today = LocalDate.now()
        val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }
        
        val dailyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last30Days }
            .groupBy { it.date }
            .mapValues { it.value.sumOf { t -> t.amount } }
        
        if (dailyExpenses.size < 7) return 50f
        
        val avg = dailyExpenses.values.average()
        val daysInBudget = dailyExpenses.values.count { it <= avg * 1.1 }
        
        return (daysInBudget.toFloat() / dailyExpenses.size * 100).coerceIn(0f, 100f)
    }

    private fun determineUserType(disciplineScore: Float, riskScore: Float, savings: Double, income: Double): UserType {
        val savingsRate = if (income > 0) (savings / income * 100) else 0.0
        
        return when {
            disciplineScore >= 70 && savingsRate >= 15 -> UserType.SAVER
            disciplineScore >= 50 && savingsRate >= 5 -> UserType.BALANCED
            riskScore >= 60 -> UserType.RISKY
            savingsRate < 0 -> UserType.SPENDER
            else -> UserType.BALANCED
        }
    }

    fun getPersonalizedTone(identity: FinancialIdentity): Tone {
        return when (identity.type) {
            UserType.SAVER -> Tone.ENCOURAGING
            UserType.BALANCED -> Tone.NEUTRAL
            UserType.SPENDER -> Tone.GENTLE_WARNING
            UserType.RISKY -> Tone.ALERT
        }
    }

    fun getRiskProfile(identity: FinancialIdentity): RiskProfile {
        return when {
            identity.riskScore >= 70 -> RiskProfile.HIGH_RISK
            identity.riskScore >= 40 -> RiskProfile.MODERATE_RISK
            else -> RiskProfile.LOW_RISK
        }
    }
}

data class FinancialIdentity(
    val type: UserType,
    val disciplineScore: Float,
    val riskScore: Float,
    val consistencyScore: Float,
    val savingsRate: Float,
    val averageSpending: Float,
    val totalTransactions: Int,
    val analyzedAt: String
)

enum class UserType {
    SAVER,
    SPENDER,
    BALANCED,
    RISKY
}

enum class Tone {
    ENCOURAGING,
    NEUTRAL,
    GENTLE_WARNING,
    ALERT
}

enum class RiskProfile {
    LOW_RISK,
    MODERATE_RISK,
    HIGH_RISK
}

data class IdentityInsight(
    val identity: FinancialIdentity,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>
)
