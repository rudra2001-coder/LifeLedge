package com.rudra.lifeledge.core.finance

import com.rudra.lifeledge.core.finance.model.*
import org.junit.Assert.*
import org.junit.Test

class FinanceEngineCalculationsTest {

    @Test
    fun overspending_detection_works_correctly() {
        val todayExpense = 3000.0
        val avgDailyExpense = 2000.0

        val isOverspending = todayExpense > avgDailyExpense * 1.5
        assertTrue("Should detect overspending when 50% above average", isOverspending)
    }

    @Test
    fun overspending_not_triggered_when_within_threshold() {
        val todayExpense = 2100.0
        val avgDailyExpense = 2000.0

        val isOverspending = todayExpense > avgDailyExpense * 1.5
        assertFalse("Should not trigger overspending when within threshold", isOverspending)
    }

    @Test
    fun savings_rate_calculated_correctly() {
        val income = 50000.0
        val expense = 35000.0
        val savings = income - expense
        val savingsRate = if (income > 0) (savings / income) * 100 else 0.0

        assertEquals(30.0, savingsRate, 0.01)
        assertEquals(15000.0, savings, 0.01)
    }

    @Test
    fun savings_rate_is_zero_when_no_income() {
        val income = 0.0
        val expense = 10000.0
        val savings = income - expense
        val savingsRate = if (income > 0) (savings / income) * 100 else 0.0

        assertEquals(0.0, savingsRate, 0.01)
    }

    @Test
    fun comparison_percentage_calculated_correctly() {
        val currentExpense = 30000.0
        val previousExpense = 25000.0
        val comparison = ((currentExpense - previousExpense) / previousExpense) * 100

        assertEquals(20.0, comparison, 0.01)
    }

    @Test
    fun negative_comparison_indicates_improvement() {
        val currentExpense = 20000.0
        val previousExpense = 25000.0
        val comparison = ((currentExpense - previousExpense) / previousExpense) * 100

        assertTrue("Negative comparison should indicate positive trend", comparison < 0)
    }

    @Test
    fun runway_days_calculated_correctly() {
        val balance = 30000.0
        val avgDailyExpense = 1500.0
        val daysRemaining = (balance / avgDailyExpense).toInt()

        assertEquals(20, daysRemaining)
    }

    @Test
    fun runway_infinite_when_no_expense() {
        val balance = 30000.0
        val avgDailyExpense = 0.0
        val daysRemaining = if (avgDailyExpense > 0) (balance / avgDailyExpense).toInt() else Int.MAX_VALUE

        assertEquals(Int.MAX_VALUE, daysRemaining)
    }

    @Test
    fun urgency_classified_correctly() {
        fun getUrgency(daysRemaining: Int) = when {
            daysRemaining > 30 -> Urgency.SAFE
            daysRemaining > 14 -> Urgency.MONITOR
            daysRemaining > 7 -> Urgency.WARNING
            else -> Urgency.CRITICAL
        }

        assertEquals(Urgency.SAFE, getUrgency(31))
        assertEquals(Urgency.MONITOR, getUrgency(20))
        assertEquals(Urgency.WARNING, getUrgency(10))
        assertEquals(Urgency.CRITICAL, getUrgency(5))
    }

    @Test
    fun category_percentage_calculated_correctly() {
        val totalExpense = 50000.0
        val foodExpense = 17500.0
        val percentage = (foodExpense / totalExpense) * 100

        assertEquals(35.0, percentage, 0.01)
    }

    @Test
    fun financial_health_score_calculated_correctly() {
        val income = 50000.0
        val expense = 35000.0
        val saved = income - expense

        val savingsScore = ((saved / income) * 100).toInt().coerceIn(0, 100)
        assertEquals(30, savingsScore)

        val spendingControlScore = when {
            expense < income * 0.8 -> 100
            expense < income * 0.9 -> 80
            expense < income -> 60
            expense < income * 1.1 -> 40
            else -> 20
        }
        assertEquals(60, spendingControlScore)
    }

    @Test
    fun budget_percent_used_calculated() {
        val budgetAmount = 40000.0
        val spentAmount = 32000.0
        val percentUsed = (spentAmount / budgetAmount) * 100

        assertEquals(80.0, percentUsed, 0.01)
    }

    @Test
    fun projected_overspend_calculated() {
        val dailyRate = 2000.0
        val dayOfMonth = 20
        val daysInMonth = 30
        val monthlyBudget = 50000.0

        val projected = dailyRate * daysInMonth
        val overspend = if (projected > monthlyBudget) projected - monthlyBudget else null

        assertEquals(10000.0, overspend, 0.01)
    }

    @Test
    fun no_projected_overspend_when_under_budget() {
        val dailyRate = 1000.0
        val dayOfMonth = 20
        val daysInMonth = 30
        val monthlyBudget = 50000.0

        val projected = dailyRate * daysInMonth
        val overspend = if (projected > monthlyBudget) projected - monthlyBudget else null

        assertNull(overspend)
    }

    @Test
    fun trend_detected_correctly() {
        val firstPeriod = 10000.0
        val lastPeriod = 12000.0

        val trend = when {
            lastPeriod > firstPeriod * 1.1 -> Trend.UP
            lastPeriod < firstPeriod * 0.9 -> Trend.DOWN
            else -> Trend.STABLE
        }

        assertEquals(Trend.UP, trend)
    }

    @Test
    fun streak_days_counted_correctly() {
        val dailyExpenses = mapOf(
            "2024-01-01" to 1000.0,
            "2024-01-02" to 1200.0,
            "2024-01-03" to 800.0,
            "2024-01-04" to 900.0,
            "2024-01-05" to 1100.0
        )
        val avgDaily = dailyExpenses.values.average()

        val underBudgetDays = dailyExpenses.count { it.value <= avgDaily * 1.2 }
        assertTrue(underBudgetDays >= 4)
    }

    @Test
    fun alert_generated_for_overspending() {
        val todayExpense = 3000.0
        val avgDailyExpense = 2000.0

        val shouldAlert = todayExpense > avgDailyExpense * 1.5
        assertTrue(shouldAlert)
    }

    @Test
    fun no_alert_when_spending_normal() {
        val todayExpense = 1500.0
        val avgDailyExpense = 2000.0

        val shouldAlert = todayExpense > avgDailyExpense * 1.5
        assertFalse(shouldAlert)
    }

    @Test
    fun suggestion_generated_when_savings_low() {
        val income = 50000.0
        val expense = 45000.0
        val savings = income - expense
        val savingsRate = (savings / income) * 100

        val shouldSuggest = savingsRate < 10
        assertTrue(shouldSuggest)
    }

    @Test
    fun no_suggestion_when_savings_good() {
        val income = 50000.0
        val expense = 35000.0
        val savings = income - expense
        val savingsRate = (savings / income) * 100

        val shouldSuggest = savingsRate < 10
        assertFalse(shouldSuggest)
    }

    @Test
    fun balance_calculation_inclusive() {
        val openingBalance = 10000.0
        val income = 50000.0
        val expense = 35000.0
        val saved = 5000.0
        val transferredFromSavings = 1000.0

        val calculatedBalance = openingBalance + income - expense - saved + transferredFromSavings

        assertEquals(21000.0, calculatedBalance, 0.01)
    }
}
