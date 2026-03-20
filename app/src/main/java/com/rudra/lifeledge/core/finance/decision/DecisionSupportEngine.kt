package com.rudra.lifeledge.core.finance.decision

import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DecisionSupportEngine {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun simulateScenario(
        transactions: List<Transaction>,
        categoryId: Long,
        reductionPercent: Double,
        months: Int = 1
    ): SimulationResult {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = today.format(dateFormatter)

        val categoryExpenses = transactions
            .filter { 
                it.type == TransactionType.EXPENSE && 
                it.categoryId == categoryId &&
                it.date >= monthStart && it.date <= monthEnd
            }
        
        val currentMonthlySpending = categoryExpenses.sumOf { it.amount }
        val reductionAmount = currentMonthlySpending * (reductionPercent / 100)
        val newMonthlySpending = currentMonthlySpending - reductionAmount
        
        val projectedAnnualSavings = reductionAmount * 12
        val projectedMonthlySavings = reductionAmount
        
        val confidence = calculateSimulationConfidence(transactions, categoryId)
        
        val risk = assessReductionRisk(reductionPercent)
        
        return SimulationResult(
            scenario = "Reduce category spending by $reductionPercent%",
            currentMonthlyAmount = currentMonthlySpending,
            projectedMonthlyAmount = newMonthlySpending,
            monthlySavings = projectedMonthlySavings,
            annualSavings = projectedAnnualSavings,
            confidence = confidence,
            riskLevel = risk,
            timeline = "Starting from next month",
            actionable = reductionPercent in 5.0..30.0,
            message = generateSimulationMessage(currentMonthlySpending, projectedAnnualSavings, reductionPercent)
        )
    }

    fun simulateIncomeIncrease(
        currentIncome: Double,
        incomeIncreasePercent: Double,
        currentSpending: Double
    ): IncomeSimulationResult {
        val newIncome = currentIncome * (1 + incomeIncreasePercent / 100)
        val newSavings = newIncome - currentSpending
        val newSavingsRate = if (newIncome > 0) (newSavings / newIncome * 100) else 0.0
        
        val currentSavingsRate = if (currentIncome > 0) ((currentIncome - currentSpending) / currentIncome * 100) else 0.0
        
        return IncomeSimulationResult(
            currentIncome = currentIncome,
            newIncome = newIncome,
            currentSavingsRate = currentSavingsRate,
            projectedSavingsRate = newSavingsRate,
            monthlySavingsIncrease = newIncome - currentIncome,
            timeToDoubleSavings = if (newSavings > currentSavingsRate / 100 * currentIncome) {
                calculateMonthsToDoubleSavings(currentIncome, currentSpending, newIncome)
            } else null,
            message = generateIncomeMessage(newSavingsRate)
        )
    }

    fun simulateBudgetScenario(
        transactions: List<Transaction>,
        monthlyBudget: Double
    ): BudgetSimulationResult {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = today.format(dateFormatter)
        val daysInMonth = today.lengthOfMonth()
        val daysPassed = today.dayOfMonth
        
        val currentSpending = transactions
            .filter { 
                it.type == TransactionType.EXPENSE && 
                it.date >= monthStart && it.date <= monthEnd 
            }
            .sumOf { it.amount }
        
        val dailyBudget = monthlyBudget / daysInMonth
        val recommendedDailySpending = (monthlyBudget - currentSpending) / (daysInMonth - daysPassed)
        
        val projectedTotal = currentSpending + (recommendedDailySpending * (daysInMonth - daysPassed))
        val overUnder = monthlyBudget - projectedTotal
        
        val daysUntilBudgetExhausted = if (recommendedDailySpending > 0) {
            ((monthlyBudget - currentSpending) / recommendedDailySpending).toInt()
        } else Int.MAX_VALUE
        
        val status = when {
            overUnder > 0 -> BudgetStatus.UNDER_BUDGET
            overUnder > -monthlyBudget * 0.1 -> BudgetStatus.ON_TRACK
            overUnder > -monthlyBudget * 0.25 -> BudgetStatus.APPROACHING_LIMIT
            else -> BudgetStatus.OVER_BUDGET
        }
        
        return BudgetSimulationResult(
            monthlyBudget = monthlyBudget,
            currentSpending = currentSpending,
            projectedTotal = projectedTotal,
            overUnder = overUnder,
            daysUntilExhausted = daysUntilBudgetExhausted,
            recommendedDailySpending = recommendedDailySpending.coerceAtLeast(0.0),
            status = status,
            message = generateBudgetMessage(status, overUnder, monthlyBudget),
            actionable = status != BudgetStatus.UNDER_BUDGET
        )
    }

    fun simulateGoalAchievement(
        currentSavings: Double,
        goalAmount: Double,
        monthlyContribution: Double,
        interestRate: Double = 0.0
    ): GoalSimulationResult {
        if (monthlyContribution <= 0) {
            return GoalSimulationResult(
                goalAmount = goalAmount,
                currentSavings = currentSavings,
                remainingAmount = goalAmount - currentSavings,
                monthsToGoal = null,
                projectedCompletionDate = null,
                isOnTrack = false,
                requiredMonthlyContribution = calculateRequiredMonthly(currentSavings, goalAmount, 12),
                message = "Increase monthly contribution to reach your goal"
            )
        }
        
        val monthsToGoal = calculateMonthsToReachGoal(currentSavings, goalAmount, monthlyContribution, interestRate)
        val projectedCompletionDate = LocalDate.now().plusMonths(monthsToGoal.toLong())
        
        val isOnTrack = monthsToGoal <= 12
        val requiredMonthly = calculateRequiredMonthly(currentSavings, goalAmount, 12)
        
        return GoalSimulationResult(
            goalAmount = goalAmount,
            currentSavings = currentSavings,
            remainingAmount = goalAmount - currentSavings,
            monthsToGoal = monthsToGoal,
            projectedCompletionDate = projectedCompletionDate.format(dateFormatter),
            isOnTrack = isOnTrack,
            requiredMonthlyContribution = requiredMonthly,
            message = generateGoalMessage(monthsToGoal, isOnTrack)
        )
    }

    private fun calculateSimulationConfidence(transactions: List<Transaction>, categoryId: Long): Double {
        val today = LocalDate.now()
        val last90Days = (0..89).map { today.minusDays(it.toLong()).format(dateFormatter) }
        
        val categoryTransactions = transactions
            .filter { it.categoryId == categoryId && it.date in last90Days }
        
        return when {
            categoryTransactions.size >= 20 -> 0.9
            categoryTransactions.size >= 10 -> 0.75
            categoryTransactions.size >= 5 -> 0.5
            else -> 0.3
        }
    }

    private fun assessReductionRisk(percent: Double): RiskLevel {
        return when {
            percent <= 10 -> RiskLevel.LOW
            percent <= 20 -> RiskLevel.MEDIUM
            else -> RiskLevel.HIGH
        }
    }

    private fun calculateMonthsToDoubleSavings(currentIncome: Double, currentSpending: Double, newIncome: Double): Int {
        val currentSavings = currentIncome - currentSpending
        if (currentSavings <= 0) return Int.MAX_VALUE
        
        val additionalSavings = newIncome - currentIncome
        return if (additionalSavings > 0) {
            ((currentSavings / additionalSavings) * 12).toInt()
        } else Int.MAX_VALUE
    }

    private fun calculateMonthsToReachGoal(current: Double, goal: Double, monthly: Double, interest: Double): Int {
        if (monthly <= 0) return Int.MAX_VALUE
        
        var balance = current
        var months = 0
        
        while (balance < goal && months < 120) {
            balance += monthly
            balance *= (1 + interest / 12 / 100)
            months++
        }
        
        return months
    }

    private fun calculateRequiredMonthly(current: Double, goal: Double, months: Int): Double {
        val remaining = goal - current
        return if (remaining > 0 && months > 0) remaining / months else 0.0
    }

    private fun generateSimulationMessage(current: Double, annual: Double, percent: Double): String {
        return "Reducing spending by $percent% would save ৳${String.format("%.0f", annual)} per year. " +
               "That's ৳${String.format("%.0f", annual / 12)} extra each month!"
    }

    private fun generateIncomeMessage(savingsRate: Double): String {
        return when {
            savingsRate >= 30 -> "Excellent! You'll be in great financial shape."
            savingsRate >= 20 -> "Good progress. Your savings rate is healthy."
            savingsRate >= 10 -> "Room for improvement. Consider ways to increase savings."
            else -> "Challenge ahead. Focus on reducing expenses."
        }
    }

    private fun generateBudgetMessage(status: BudgetStatus, overUnder: Double, budget: Double): String {
        return when (status) {
            BudgetStatus.UNDER_BUDGET -> "You're doing great! ৳${String.format("%.0f", -overUnder)} under budget."
            BudgetStatus.ON_TRACK -> "You're on track. Keep monitoring your spending."
            BudgetStatus.APPROACHING_LIMIT -> "Warning: You'll approach budget limit soon."
            BudgetStatus.OVER_BUDGET -> "Action needed: ৳${String.format("%.0f", overUnder)} over projected. Reduce daily spending."
        }
    }

    private fun generateGoalMessage(months: Int, isOnTrack: Boolean): String {
        return when {
            months == Int.MAX_VALUE -> "Increase contribution to reach goal"
            isOnTrack -> "Great! You'll reach your goal in $months months."
            else -> "Consider increasing monthly contribution to reach goal faster."
        }
    }
}

data class SimulationResult(
    val scenario: String,
    val currentMonthlyAmount: Double,
    val projectedMonthlyAmount: Double,
    val monthlySavings: Double,
    val annualSavings: Double,
    val confidence: Double,
    val riskLevel: RiskLevel,
    val timeline: String,
    val actionable: Boolean,
    val message: String
)

data class IncomeSimulationResult(
    val currentIncome: Double,
    val newIncome: Double,
    val currentSavingsRate: Double,
    val projectedSavingsRate: Double,
    val monthlySavingsIncrease: Double,
    val timeToDoubleSavings: Int?,
    val message: String
)

data class BudgetSimulationResult(
    val monthlyBudget: Double,
    val currentSpending: Double,
    val projectedTotal: Double,
    val overUnder: Double,
    val daysUntilExhausted: Int,
    val recommendedDailySpending: Double,
    val status: BudgetStatus,
    val message: String,
    val actionable: Boolean
)

data class GoalSimulationResult(
    val goalAmount: Double,
    val currentSavings: Double,
    val remainingAmount: Double,
    val monthsToGoal: Int?,
    val projectedCompletionDate: String?,
    val isOnTrack: Boolean,
    val requiredMonthlyContribution: Double,
    val message: String
)

enum class RiskLevel { LOW, MEDIUM, HIGH }
enum class BudgetStatus { UNDER_BUDGET, ON_TRACK, APPROACHING_LIMIT, OVER_BUDGET }
