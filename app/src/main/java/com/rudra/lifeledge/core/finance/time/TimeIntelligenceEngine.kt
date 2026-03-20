package com.rudra.lifeledge.core.finance.time

import com.rudra.lifeledge.core.finance.insight.Priority
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class TimeIntelligenceEngine {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTimeContext(): TimeContext {
        val today = LocalDate.now()
        val month = YearMonth.from(today)
        
        val phase = determineMonthPhase(today, month)
        val period = determineTimePeriod(today)
        
        return TimeContext(
            date = today.format(dateFormatter),
            phase = phase,
            period = period,
            dayOfMonth = today.dayOfMonth,
            daysInMonth = month.lengthOfMonth(),
            daysRemainingInMonth = month.lengthOfMonth() - today.dayOfMonth,
            weekOfMonth = (today.dayOfMonth - 1) / 7 + 1,
            dayOfWeek = today.dayOfWeek.name
        )
    }

    fun analyzeTimePatterns(transactions: List<Transaction>): TimeAnalysis {
        val context = getTimeContext()
        
        val monthAnalysis = analyzeMonthProgress(transactions)
        val weekAnalysis = analyzeWeekProgress(transactions)
        val historicalAnalysis = analyzeHistoricalPatterns(transactions)
        
        return TimeAnalysis(
            context = context,
            monthProgress = monthAnalysis,
            weekProgress = weekAnalysis,
            historicalPatterns = historicalAnalysis,
            recommendations = generateTimeBasedRecommendations(context, monthAnalysis, weekAnalysis)
        )
    }

    private fun determineMonthPhase(today: LocalDate, month: YearMonth): MonthPhase {
        val dayOfMonth = today.dayOfMonth
        val totalDays = month.lengthOfMonth()
        val progress = dayOfMonth.toFloat() / totalDays
        
        return when {
            progress < 0.1 -> MonthPhase.START
            progress < 0.5 -> MonthPhase.EARLY_MID
            progress < 0.75 -> MonthPhase.LATE_MID
            progress < 0.95 -> MonthPhase.END
            else -> MonthPhase.FINAL_DAYS
        }
    }

    private fun determineTimePeriod(today: LocalDate): TimePeriod {
        val hour = LocalDateTime.now().hour
        
        return when (hour) {
            in 5..11 -> TimePeriod.MORNING
            in 12..16 -> TimePeriod.AFTERNOON
            in 17..20 -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }

    private fun analyzeMonthProgress(transactions: List<Transaction>): MonthProgress {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        
        val income = transactions
            .filter { it.type == TransactionType.INCOME && it.date >= monthStart }
            .sumOf { it.amount }
        
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= monthStart }
            .sumOf { it.amount }
        
        val dayOfMonth = today.dayOfMonth
        val daysInMonth = today.lengthOfMonth()
        
        val expectedProgress = dayOfMonth.toFloat() / daysInMonth
        val actualProgress = if (income > 0) (expenses / income).toFloat() else 0f
        
        val status = when {
            actualProgress < expectedProgress * 0.8 -> ProgressStatus.UNDER_BUDGET
            actualProgress < expectedProgress * 1.1 -> ProgressStatus.ON_TRACK
            actualProgress < expectedProgress * 1.25 -> ProgressStatus.APPROACHING_LIMIT
            else -> ProgressStatus.OVER_BUDGET
        }
        
        val remainingBudget = income - expenses
        val dailyRemaining = if (today.lengthOfMonth() - dayOfMonth > 0) {
            remainingBudget / (today.lengthOfMonth() - dayOfMonth)
        } else 0.0
        
        return MonthProgress(
            income = income,
            expenses = expenses,
            savings = income - expenses,
            savingsRate = if (income > 0) (income - expenses) / income * 100 else 0.0,
            daysPassed = dayOfMonth,
            daysRemaining = daysInMonth - dayOfMonth,
            expectedProgress = expectedProgress * 100,
            actualProgress = actualProgress * 100,
            status = status,
            remainingBudget = remainingBudget,
            recommendedDailySpending = dailyRemaining
        )
    }

    private fun analyzeWeekProgress(transactions: List<Transaction>): WeekProgress {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(dateFormatter)
        
        val weekExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= startOfWeek }
            .sumOf { it.amount }
        
        val daysPassedInWeek = today.dayOfWeek.value
        val dailyAverage = weekExpenses / daysPassedInWeek
        
        return WeekProgress(
            expensesSoFar = weekExpenses,
            daysPassed = daysPassedInWeek,
            dailyAverage = dailyAverage,
            projectedWeeklyTotal = dailyAverage * 7,
            isOnTrack = dailyAverage * 7 <= dailyAverage * 7
        )
    }

    private fun analyzeHistoricalPatterns(transactions: List<Transaction>): HistoricalPatterns {
        val today = LocalDate.now()
        
        val dayOfWeekSpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { LocalDate.parse(it.date).dayOfWeek }
            .mapValues { it.value.sumOf { t -> t.amount } }
        
        val highestSpendingDay = dayOfWeekSpending.maxByOrNull { it.value }?.key
        val lowestSpendingDay = dayOfWeekSpending.minByOrNull { it.value }?.key
        
        val monthOverMonth = calculateMonthOverMonth(transactions)
        
        return HistoricalPatterns(
            highestSpendingDay = highestSpendingDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A",
            lowestSpendingDay = lowestSpendingDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A",
            monthOverMonthChange = monthOverMonth,
            averageDailySpending = if (dayOfWeekSpending.isNotEmpty()) dayOfWeekSpending.values.average() else 0.0
        )
    }

    private fun calculateMonthOverMonth(transactions: List<Transaction>): Double {
        val today = LocalDate.now()
        val thisMonthStart = today.withDayOfMonth(1).format(dateFormatter)
        val lastMonthStart = today.minusMonths(1).withDayOfMonth(1).format(dateFormatter)
        val lastMonthEnd = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).format(dateFormatter)
        
        val thisMonthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= thisMonthStart }
            .sumOf { it.amount }
        
        val lastMonthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date >= lastMonthStart && it.date <= lastMonthEnd }
            .sumOf { it.amount }
        
        return if (lastMonthExpenses > 0) {
            ((thisMonthExpenses - lastMonthExpenses) / lastMonthExpenses * 100)
        } else 0.0
    }

    private fun generateTimeBasedRecommendations(
        context: TimeContext,
        monthProgress: MonthProgress,
        weekProgress: WeekProgress
    ): List<TimeRecommendation> {
        val recommendations = mutableListOf<TimeRecommendation>()
        
        when (context.phase) {
            MonthPhase.START -> {
                recommendations.add(
                    TimeRecommendation(
                        type = RecommendationType.PLANNING,
                        title = "Month Start - Planning Phase",
                        message = "This is the perfect time to set your monthly budget and spending limits.",
                        priority = Priority.HIGH,
                        actionable = true
                    )
                )
            }
            MonthPhase.EARLY_MID -> {
                if (monthProgress.status == ProgressStatus.OVER_BUDGET) {
                    recommendations.add(
                        TimeRecommendation(
                            type = RecommendationType.COURSE_CORRECTION,
                            title = "Course Correction Needed",
                            message = "You're over budget already. Reduce daily spending to ৳${String.format("%.0f", monthProgress.recommendedDailySpending)} to stay on track.",
                            priority = Priority.HIGH,
                            actionable = true
                        )
                    )
                }
            }
            MonthPhase.LATE_MID -> {
                recommendations.add(
                    TimeRecommendation(
                        type = RecommendationType.MID_MONTH_CHECK,
                        title = "Mid-Month Check",
                        message = "You're ${String.format("%.0f", context.daysRemainingInMonth)} days from month end. " +
                                 "Current savings rate: ${String.format("%.1f", monthProgress.savingsRate)}%",
                        priority = Priority.MEDIUM,
                        actionable = true
                    )
                )
            }
            MonthPhase.END -> {
                recommendations.add(
                    TimeRecommendation(
                        type = RecommendationType.MONTH_END_PREP,
                        title = "Month End Approaching",
                        message = "Only ${context.daysRemainingInMonth} days left! Final push to meet your savings goal.",
                        priority = Priority.HIGH,
                        actionable = true
                    )
                )
            }
            MonthPhase.FINAL_DAYS -> {
                val projectedSavings = monthProgress.savings + (monthProgress.recommendedDailySpending * context.daysRemainingInMonth)
                recommendations.add(
                    TimeRecommendation(
                        type = RecommendationType.FINAL_PUSH,
                        title = "Final ${context.daysRemainingInMonth} Days",
                        message = "Projected savings: ৳${String.format("%.0f", projectedSavings)}. " +
                                 "Stay under ৳${String.format("%.0f", monthProgress.recommendedDailySpending)}/day to finish strong!",
                        priority = Priority.CRITICAL,
                        actionable = true
                    )
                )
            }
        }
        
        return recommendations
    }

    fun generateTimeAwareMessages(context: TimeContext): List<String> {
        val messages = mutableListOf<String>()
        
        when (context.period) {
            TimePeriod.MORNING -> {
                messages.add("Good morning! Here's your financial snapshot for today.")
            }
            TimePeriod.AFTERNOON -> {
                messages.add("Afternoon check: You're doing ${getProgressAdjective(context)} today.")
            }
            TimePeriod.EVENING -> {
                messages.add("Evening review: ${getEveningMessage(context)}")
            }
            TimePeriod.NIGHT -> {
                messages.add("Tomorrow is a new day! Plan your finances tonight.")
            }
        }
        
        return messages
    }

    private fun getProgressAdjective(context: TimeContext): String {
        return when (context.phase) {
            MonthPhase.START -> "great"
            MonthPhase.EARLY_MID -> "well"
            MonthPhase.LATE_MID -> "okay"
            MonthPhase.END -> "alright"
            MonthPhase.FINAL_DAYS -> "focused"
        }
    }

    private fun getEveningMessage(context: TimeContext): String {
        return when (context.phase) {
            MonthPhase.START -> "You've got the whole month ahead!"
            MonthPhase.EARLY_MID -> "${context.daysRemainingInMonth} days left this month. Keep going!"
            MonthPhase.LATE_MID -> "Halfway point passed. ${context.daysRemainingInMonth} days to go!"
            MonthPhase.END -> "End of month approaching. Final push!"
            MonthPhase.FINAL_DAYS -> "Last ${context.daysRemainingInMonth} days! Make them count!"
        }
    }
}

data class TimeContext(
    val date: String,
    val phase: MonthPhase,
    val period: TimePeriod,
    val dayOfMonth: Int,
    val daysInMonth: Int,
    val daysRemainingInMonth: Int,
    val weekOfMonth: Int,
    val dayOfWeek: String
)

enum class MonthPhase { START, EARLY_MID, LATE_MID, END, FINAL_DAYS }
enum class TimePeriod { MORNING, AFTERNOON, EVENING, NIGHT }

data class TimeAnalysis(
    val context: TimeContext,
    val monthProgress: MonthProgress,
    val weekProgress: WeekProgress,
    val historicalPatterns: HistoricalPatterns,
    val recommendations: List<TimeRecommendation>
)

data class MonthProgress(
    val income: Double,
    val expenses: Double,
    val savings: Double,
    val savingsRate: Double,
    val daysPassed: Int,
    val daysRemaining: Int,
    val expectedProgress: Float,
    val actualProgress: Float,
    val status: ProgressStatus,
    val remainingBudget: Double,
    val recommendedDailySpending: Double
)

enum class ProgressStatus { UNDER_BUDGET, ON_TRACK, APPROACHING_LIMIT, OVER_BUDGET }

data class WeekProgress(
    val expensesSoFar: Double,
    val daysPassed: Int,
    val dailyAverage: Double,
    val projectedWeeklyTotal: Double,
    val isOnTrack: Boolean
)

data class HistoricalPatterns(
    val highestSpendingDay: String,
    val lowestSpendingDay: String,
    val monthOverMonthChange: Double,
    val averageDailySpending: Double
)

data class TimeRecommendation(
    val type: RecommendationType,
    val title: String,
    val message: String,
    val priority: Priority,
    val actionable: Boolean
)

enum class RecommendationType {
    PLANNING,
    MID_MONTH_CHECK,
    COURSE_CORRECTION,
    MONTH_END_PREP,
    FINAL_PUSH
}
