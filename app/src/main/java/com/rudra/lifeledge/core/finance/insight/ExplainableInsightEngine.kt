package com.rudra.lifeledge.core.finance.insight

import com.rudra.lifeledge.core.finance.identity.FinancialIdentity
import com.rudra.lifeledge.core.finance.identity.FinancialIdentityEngine
import com.rudra.lifeledge.core.finance.identity.Tone
import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExplainableInsightEngine {

    private val identityEngine = FinancialIdentityEngine()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun generateInsights(transactions: List<Transaction>): List<ExplainableInsight> {
        val insights = mutableListOf<ExplainableInsight>()
        val identity = identityEngine.analyzeIdentity(transactions)
        
        insights.addAll(analyzeTodaySpending(transactions, identity))
        insights.addAll(analyzeWeeklyTrend(transactions))
        insights.addAll(analyzeCategoryPatterns(transactions))
        insights.addAll(analyzeSavingsProgress(transactions))
        
        return insights.sortedByDescending { it.priority }
    }

    private fun analyzeTodaySpending(transactions: List<Transaction>, identity: FinancialIdentity): List<ExplainableInsight> {
        val insights = mutableListOf<ExplainableInsight>()
        val today = LocalDate.now().format(dateFormatter)
        
        val todayExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date == today }
            .sumOf { it.amount }
        
        if (todayExpenses == 0.0) {
            insights.add(
                ExplainableInsight(
                    type = InsightType.TODAY_SPENDING,
                    title = "No spending today",
                    shortMessage = "You haven't spent anything today",
                    message = "Great job! You haven't made any expenses today. This is a good opportunity to save.",
                    reason = "Zero transactions recorded for today",
                    data = mapOf("today_expense" to 0.0),
                    priority = Priority.LOW,
                    actionable = false
                )
            )
            return insights
        }
        
        val last7Days = (1..7).map { LocalDate.now().minusDays(it.toLong()).format(dateFormatter) }
        val lastWeekAvg = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date in last7Days }
            .sumOf { it.amount } / 7
        
        val percentDiff = if (lastWeekAvg > 0) ((todayExpenses - lastWeekAvg) / lastWeekAvg * 100) else 0.0
        
        when {
            percentDiff > 50 -> {
                insights.add(
                    ExplainableInsight(
                        type = InsightType.TODAY_SPENDING,
                        title = "High spending today",
                        shortMessage = "৳${String.format("%.0f", todayExpenses)} spent today",
                        message = generateEmotionAwareMessage(identity, "high_today", todayExpenses, lastWeekAvg),
                        reason = "Today: ৳${String.format("%.0f", todayExpenses)} vs Average: ৳${String.format("%.0f", lastWeekAvg)} (+${String.format("%.0f", percentDiff)}%)",
                        data = mapOf(
                            "today_expense" to todayExpenses,
                            "weekly_average" to lastWeekAvg,
                            "percent_difference" to percentDiff
                        ),
                        priority = Priority.HIGH,
                        actionable = true,
                        actionLabel = "Review spending"
                    )
                )
            }
            percentDiff > 20 -> {
                insights.add(
                    ExplainableInsight(
                        type = InsightType.TODAY_SPENDING,
                        title = "Elevated spending today",
                        shortMessage = "Slightly above average",
                        message = "You're spending a bit more than usual today. ${getEmotionAwareSuggestion(identity, "elevated")}",
                        reason = "Today is ${String.format("%.0f", percentDiff)}% above your 7-day average",
                        data = mapOf(
                            "today_expense" to todayExpenses,
                            "weekly_average" to lastWeekAvg,
                            "percent_difference" to percentDiff
                        ),
                        priority = Priority.MEDIUM,
                        actionable = true,
                        actionLabel = "View details"
                    )
                )
            }
            percentDiff < -30 -> {
                insights.add(
                    ExplainableInsight(
                        type = InsightType.TODAY_SPENDING,
                        title = "Great spending day!",
                        shortMessage = "Way below average",
                        message = getEmotionAwareCelebration(identity, "below_average"),
                        reason = "Today: ৳${String.format("%.0f", todayExpenses)} vs Average: ৳${String.format("%.0f", lastWeekAvg)} (${String.format("%.0f", percentDiff)}%)",
                        data = mapOf(
                            "today_expense" to todayExpenses,
                            "weekly_average" to lastWeekAvg,
                            "percent_difference" to percentDiff
                        ),
                        priority = Priority.MEDIUM,
                        actionable = false
                    )
                )
            }
        }
        
        return insights
    }

    private fun analyzeWeeklyTrend(transactions: List<Transaction>): List<ExplainableInsight> {
        val insights = mutableListOf<ExplainableInsight>()
        val today = LocalDate.now()
        
        val thisWeekStart = today.minusDays(6).format(dateFormatter)
        val lastWeekStart = today.minusDays(13).format(dateFormatter)
        
        val thisWeekExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= thisWeekStart }
            .sumOf { it.amount }
        
        val lastWeekExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= lastWeekStart && it.date < thisWeekStart }
            .sumOf { it.amount }
        
        if (lastWeekExpenses > 0) {
            val change = ((thisWeekExpenses - lastWeekExpenses) / lastWeekExpenses * 100)
            
            when {
                change > 20 -> {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.WEEKLY_TREND,
                            title = "Spending increased this week",
                            shortMessage = "${String.format("%.0f", change)}% more than last week",
                            message = "Your weekly spending is ${String.format("%.0f", change)}% higher than last week. " +
                                     "This could affect your monthly savings goal.",
                            reason = "This week: ৳${String.format("%.0f", thisWeekExpenses)} vs Last week: ৳${String.format("%.0f", lastWeekExpenses)}",
                            data = mapOf(
                                "this_week" to thisWeekExpenses,
                                "last_week" to lastWeekExpenses,
                                "change_percent" to change
                            ),
                            priority = Priority.HIGH,
                            actionable = true,
                            actionLabel = "View breakdown"
                        )
                    )
                }
                change < -20 -> {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.WEEKLY_TREND,
                            title = "Great week! Spending down",
                            shortMessage = "${String.format("%.0f", -change)}% less than last week",
                            message = "Excellent! You've spent ${String.format("%.0f", -change)}% less than last week. " +
                                     "Keep up this momentum!",
                            reason = "This week: ৳${String.format("%.0f", thisWeekExpenses)} vs Last week: ৳${String.format("%.0f", lastWeekExpenses)}",
                            data = mapOf(
                                "this_week" to thisWeekExpenses,
                                "last_week" to lastWeekExpenses,
                                "change_percent" to change
                            ),
                            priority = Priority.MEDIUM,
                            actionable = false
                        )
                    )
                }
            }
        }
        
        return insights
    }

    private fun analyzeCategoryPatterns(transactions: List<Transaction>): List<ExplainableInsight> {
        val insights = mutableListOf<ExplainableInsight>()
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        
        val categorySpending = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= monthStart }
            .groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
        
        val totalSpending = categorySpending.values.sum()
        
        if (totalSpending > 0) {
            val topCategory = categorySpending.maxByOrNull { it.value }
            if (topCategory != null) {
                val percentage = (topCategory.value / totalSpending * 100)
                
                if (percentage > 40) {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.CATEGORY_ALERT,
                            title = "High category concentration",
                            shortMessage = "${String.format("%.0f", percentage)}% in one category",
                            message = "You're spending ${String.format("%.0f", percentage)}% of your money in one category. " +
                                     "Reducing this by 10% could save ৳${String.format("%.0f", topCategory.value * 0.1)}/month.",
                            reason = "Top category accounts for ${String.format("%.0f", percentage)}% of total spending",
                            data = mapOf(
                                "category_amount" to topCategory.value,
                                "category_percentage" to percentage,
                                "potential_savings" to topCategory.value * 0.1
                            ),
                            priority = Priority.MEDIUM,
                            actionable = true,
                            actionLabel = "View category"
                        )
                    )
                }
            }
        }
        
        return insights
    }

    private fun analyzeSavingsProgress(transactions: List<Transaction>): List<ExplainableInsight> {
        val insights = mutableListOf<ExplainableInsight>()
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        
        val income = transactions
            .filter { it.type == TransactionType.INCOME && it.date >= monthStart }
            .sumOf { it.amount }
        
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= monthStart }
            .sumOf { it.amount }
        
        if (income > 0) {
            val savingsRate = ((income - expenses) / income * 100)
            
            when {
                savingsRate < 0 -> {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.SAVINGS_ALERT,
                            title = "Negative savings rate",
                            shortMessage = "Spending exceeds income",
                            message = "This month, you've spent more than you've earned. " +
                     "Review your expenses to get back on track.",
                            reason = "Income: ৳${String.format("%.0f", income)} - Expenses: ৳${String.format("%.0f", expenses)} = ৳${String.format("%.0f", income - expenses)}",
                            data = mapOf(
                                "income" to income,
                                "expenses" to expenses,
                                "savings_rate" to savingsRate
                            ),
                            priority = Priority.CRITICAL,
                            actionable = true,
                            actionLabel = "Review budget"
                        )
                    )
                }
                savingsRate < 10 -> {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.SAVINGS_ALERT,
                            title = "Low savings rate",
                            shortMessage = "Only ${String.format("%.0f", savingsRate)}% saved",
                            message = "Your savings rate is below recommended 20%. " +
                     "Try to save at least ৳${String.format("%.0f", income * 0.2 - (income - expenses))} more this month.",
                            reason = "Current savings rate: ${String.format("%.0f", savingsRate)}% (recommended: 20%)",
                            data = mapOf(
                                "income" to income,
                                "expenses" to expenses,
                                "savings_rate" to savingsRate,
                                "target_savings" to income * 0.2
                            ),
                            priority = Priority.HIGH,
                            actionable = true,
                            actionLabel = "View tips"
                        )
                    )
                }
                savingsRate >= 20 -> {
                    insights.add(
                        ExplainableInsight(
                            type = InsightType.SAVINGS_CELEBRATION,
                            title = "Excellent savings rate!",
                            shortMessage = "${String.format("%.0f", savingsRate)}% saved",
                            message = "You're doing amazing! Saving ${String.format("%.0f", savingsRate)}% of your income " +
                     "puts you in excellent financial health.",
                            reason = "Your savings rate of ${String.format("%.0f", savingsRate)}% exceeds the recommended 20%",
                            data = mapOf(
                                "income" to income,
                                "expenses" to expenses,
                                "savings_rate" to savingsRate
                            ),
                            priority = Priority.LOW,
                            actionable = false
                        )
                    )
                }
            }
        }
        
        return insights
    }

    private fun generateEmotionAwareMessage(identity: FinancialIdentity, situation: String, today: Double, avg: Double): String {
        val tone = identityEngine.getPersonalizedTone(identity)
        
        return when (situation) {
            "high_today" -> when (tone) {
                Tone.ENCOURAGING -> "Don't worry! One day won't ruin your progress. Let's get back on track tomorrow."
                Tone.NEUTRAL -> "You're spending more than usual today. Keep it in mind for the rest of the month."
                Tone.GENTLE_WARNING -> "Looks like today was expensive. Maybe take it easy tomorrow?"
                Tone.ALERT -> "Warning: Today's spending is ${String.format("%.0f", (today/avg-1)*100)}% above average. This affects your savings goal."
            }
            else -> "Let's be mindful of spending."
        }
    }

    private fun getEmotionAwareSuggestion(identity: FinancialIdentity, situation: String): String {
        return when (situation) {
            "elevated" -> when (identity.type) {
                com.rudra.lifeledge.core.finance.identity.UserType.SAVER -> "You've been great with money. This is just a minor spike."
                com.rudra.lifeledge.core.finance.identity.UserType.BALANCED -> "It's okay to treat yourself occasionally. Just be mindful."
                com.rudra.lifeledge.core.finance.identity.UserType.SPENDER -> "This might affect your monthly savings. Try to cut back tomorrow."
                com.rudra.lifeledge.core.finance.identity.UserType.RISKY -> "This spending pattern could lead to issues. Consider reviewing your budget."
            }
            else -> "Keep up the good work!"
        }
    }

    private fun getEmotionAwareCelebration(identity: FinancialIdentity, situation: String): String {
        return when (identity.type) {
            com.rudra.lifeledge.core.finance.identity.UserType.SAVER -> "Your discipline is paying off! You're a natural saver."
            com.rudra.lifeledge.core.finance.identity.UserType.BALANCED -> "Great job! Balanced spending like this leads to financial freedom."
            com.rudra.lifeledge.core.finance.identity.UserType.SPENDER -> "Look at you! Even small wins matter. Keep it going!"
            com.rudra.lifeledge.core.finance.identity.UserType.RISKY -> "This is the way! Consistent controlled spending builds great habits."
        }
    }
}

data class ExplainableInsight(
    val type: InsightType,
    val title: String,
    val shortMessage: String,
    val message: String,
    val reason: String,
    val data: Map<String, Any>,
    val priority: Priority,
    val actionable: Boolean,
    val actionLabel: String? = null
)

enum class InsightType {
    TODAY_SPENDING,
    WEEKLY_TREND,
    MONTHLY_SUMMARY,
    CATEGORY_ALERT,
    SAVINGS_ALERT,
    SAVINGS_CELEBRATION,
    BUDGET_STATUS,
    GOAL_PROGRESS
}

enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }
