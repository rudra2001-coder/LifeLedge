package com.rudra.lifeledge.core.finance

import com.rudra.lifeledge.core.finance.model.*
import com.rudra.lifeledge.data.local.dao.*
import com.rudra.lifeledge.data.local.entity.*
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

class FinanceEngine(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val monthlySummaryDao: MonthlySummaryDao? = null,
    private val dailySummaryDao: DailySummaryDao? = null,
    private val categorySummaryDao: CategorySummaryDao? = null,
    private val accountSummaryDao: AccountSummaryDao? = null,
    private val behaviorPatternDao: BehaviorPatternDao? = null,
    private val spendingStreakDao: SpendingStreakDao? = null
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @androidx.room.Transaction
    suspend fun insertTransactionWithIntegrity(transaction: Transaction): Long {
        val transactionId = transactionDao.insertTransaction(transaction)
        updateSummariesForTransaction(transaction)
        return transactionId
    }

    @androidx.room.Transaction
    suspend fun deleteTransactionWithIntegrity(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        updateSummariesForTransactionDeletion(transaction)
    }

    @androidx.room.Transaction
    suspend fun updateTransactionWithIntegrity(oldTransaction: Transaction, newTransaction: Transaction) {
        transactionDao.updateTransaction(newTransaction)
        updateSummariesForTransactionDeletion(oldTransaction)
        updateSummariesForTransaction(newTransaction)
    }

    private suspend fun updateSummariesForTransaction(transaction: Transaction) {
        val date = LocalDate.parse(transaction.date)
        val yearMonth = YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy-MM"))

        dailySummaryDao?.let { dao ->
            val existing = dao.getDailySummary(transaction.date)
            val income = transactionDao.getDailyIncome(transaction.date).first()
            val expense = transactionDao.getDailyExpense(transaction.date).first()
            val count = transactionDao.getTransactionsForDate(transaction.date).first().size

            dao.insertDailySummary(
                DailySummaryEntity(
                    date = transaction.date,
                    income = income,
                    expense = expense,
                    savings = income - expense,
                    transactionCount = count,
                    lastUpdated = LocalDate.now().format(dateFormatter)
                )
            )
        }

        monthlySummaryDao?.let { dao ->
            val monthStart = date.withDayOfMonth(1).format(dateFormatter)
            val monthEnd = date.format(dateFormatter)
            val income = transactionDao.getMonthlyIncome(monthStart, monthEnd).first()
            val expense = transactionDao.getMonthlyExpense(monthStart, monthEnd).first()
            val count = transactionDao.getTransactionsBetween(monthStart, monthEnd).first().size

            dao.insertMonthlySummary(
                MonthlySummaryEntity(
                    yearMonth = yearMonth,
                    income = income,
                    expense = expense,
                    savings = income - expense,
                    savingsRate = if (income > 0) ((income - expense) / income) * 100 else 0.0,
                    transactionCount = count,
                    lastUpdated = LocalDate.now().format(dateFormatter)
                )
            )
        }
    }

    private suspend fun updateSummariesForTransactionDeletion(transaction: Transaction) {
        updateSummariesForTransaction(transaction)
    }

    suspend fun validateIntegrity(): IntegrityCheck {
        val accounts = accountDao.getAllAccounts().first()
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()
        val mismatches = mutableListOf<AccountMismatch>()

        for (account in accounts) {
            val accountTransactions = transactions.filter { it.accountId == account.id }
            val calculatedBalance = account.balance +
                    accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } -
                    accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
                    accountTransactions.filter { it.type == TransactionType.SAVE }.sumOf { it.amount } +
                    accountTransactions.filter { it.type == TransactionType.TRANSFER_FROM_SAVING }.sumOf { it.amount }

            if (kotlin.math.abs(calculatedBalance - account.balance) > 0.01) {
                mismatches.add(
                    AccountMismatch(
                        accountId = account.id,
                        accountName = account.name,
                        calculatedBalance = calculatedBalance,
                        storedBalance = account.balance,
                        difference = calculatedBalance - account.balance
                    )
                )
            }
        }

        return IntegrityCheck(
            isValid = mismatches.isEmpty(),
            accountMismatches = mismatches,
            transactionCount = transactions.size.toLong(),
            lastVerified = LocalDate.now().format(dateFormatter)
        )
    }

    suspend fun fixIntegrityIssues() {
        val accounts = accountDao.getAllAccounts().first()
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()

        for (account in accounts) {
            val accountTransactions = transactions.filter { it.accountId == account.id }
            val calculatedBalance = account.balance +
                    accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } -
                    accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
                    accountTransactions.filter { it.type == TransactionType.SAVE }.sumOf { it.amount } +
                    accountTransactions.filter { it.type == TransactionType.TRANSFER_FROM_SAVING }.sumOf { it.amount }

            accountDao.updateAccount(account.copy(balance = calculatedBalance))
        }
    }

    fun getNetBalance(): Flow<Double> = transactionDao.getNetBalance()
    fun getTotalIncome(): Flow<Double> = transactionDao.getTotalIncome()
    fun getTotalExpense(): Flow<Double> = transactionDao.getTotalExpense()
    fun getTotalSaved(): Flow<Double> = transactionDao.getSavingsBalance()

    fun getSpendingPrediction(daysToPredict: Int = 7): Flow<SpendingPrediction> {
        val today = LocalDate.now()
        val startDate = today.minusDays(30).format(dateFormatter)
        val endDate = today.format(dateFormatter)

        return transactionDao.getTransactionsBetween(startDate, endDate).map { transactions ->
            val dailyExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.date }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }

            val last7Days = (0..6).map { today.minusDays(it.toLong()).format(dateFormatter) }
            val last7Expenses = last7Days.map { dailyExpenses[it] ?: 0.0 }

            val weightedAvg = last7Expenses.mapIndexed { index, amount ->
                val weight = (index + 1).toDouble() / 7
                amount * weight
            }.sum() / 7

            val simpleAvg = if (last7Expenses.isNotEmpty()) last7Expenses.average() else 0.0

            val first3Avg = last7Expenses.take(3).average()
            val last4Avg = last7Expenses.takeLast(4).average()
            val trend = if (last4Avg > first3Avg * 1.1) Trend.UP else if (last4Avg < first3Avg * 0.9) Trend.DOWN else Trend.STABLE

            val predictedWeekly = weightedAvg * daysToPredict
            val confidence = if (dailyExpenses.size >= 20) 0.9 else if (dailyExpenses.size >= 10) 0.7 else 0.5

            val model = when {
                dailyExpenses.size >= 14 -> PredictionModel.LINEAR_TREND
                dailyExpenses.size >= 7 -> PredictionModel.WEIGHTED_RECENT
                else -> PredictionModel.SIMPLE_AVERAGE
            }

            val message = when (model) {
                PredictionModel.LINEAR_TREND -> "Based on your 30-day spending pattern"
                PredictionModel.WEIGHTED_RECENT -> "Based on your recent spending trends"
                PredictionModel.SIMPLE_AVERAGE -> "Limited data - prediction may vary"
            }

            SpendingPrediction(
                predictedWeeklyExpense = predictedWeekly,
                confidence = confidence,
                basedOnDays = dailyExpenses.size,
                dailyAverage = weightedAvg,
                predictionModel = model,
                weeklyTrend = trend,
                message = message
            )
        }
    }

    fun getRunwayCalculation(): Flow<RunwayCalculation> {
        return combine(
            transactionDao.getNetBalance(),
            getSpendingPrediction(1)
        ) { balance, prediction ->
            val avgDaily = prediction.dailyAverage
            val daysRemaining = if (avgDaily > 0) (balance / avgDaily).toInt() else Int.MAX_VALUE

            val projectedZeroDate = if (avgDaily > 0 && daysRemaining > 0) {
                LocalDate.now().plusDays(daysRemaining.toLong()).format(dateFormatter)
            } else null

            val urgency = when {
                daysRemaining > 30 -> Urgency.SAFE
                daysRemaining > 14 -> Urgency.MONITOR
                daysRemaining > 7 -> Urgency.WARNING
                else -> Urgency.CRITICAL
            }

            val message = when (urgency) {
                Urgency.SAFE -> "Your balance is healthy. Keep it up!"
                Urgency.MONITOR -> "Monitor your spending. You have ~$daysRemaining days remaining."
                Urgency.WARNING -> "Warning: Low balance! Reduce non-essential spending."
                Urgency.CRITICAL -> "Critical: Running out soon! Take immediate action."
            }

            RunwayCalculation(
                currentBalance = balance,
                averageDailyExpense = avgDaily,
                daysRemaining = daysRemaining,
                projectedZeroDate = projectedZeroDate,
                urgency = urgency,
                message = message
            )
        }
    }

    fun getSmartSuggestions(): Flow<List<SmartSuggestion>> {
        val monthStart = LocalDate.now().withDayOfMonth(1).format(dateFormatter)
        val monthEnd = LocalDate.now().format(dateFormatter)

        return combine(
            transactionDao.getMonthlyIncome(monthStart, monthEnd),
            transactionDao.getMonthlyExpense(monthStart, monthEnd),
            getSpendingPrediction(),
            getCategorySpending(LocalDate.now()),
            getTotalSaved()
        ) { income, expense, prediction, categorySpending, savings ->
            val suggestions = mutableListOf<SmartSuggestion>()
            val savings = income - expense
            val savingsRate = if (income > 0) (savings / income) * 100 else 0.0

            if (savingsRate < 10 && income > 0) {
                val targetSavings = income * 0.2
                val needed = targetSavings - savings
                suggestions.add(
                    SmartSuggestion(
                        type = SuggestionType.SAVING_OPPORTUNITY,
                        title = "Increase Savings Rate",
                        description = "Try to save ৳${String.format("%.0f", needed)} more to reach 20% savings rate",
                        potentialSavings = needed,
                        actionRequired = false,
                        priority = 1,
                        icon = "savings"
                    )
                )
            }

            val topCategory = categorySpending.maxByOrNull { it.amount }
            if (topCategory != null && topCategory.percentage > 30) {
                val potentialSaving = topCategory.amount * 0.1
                suggestions.add(
                    SmartSuggestion(
                        type = SuggestionType.REDUCE_SPENDING,
                        title = "Reduce ${topCategory.categoryName} Spending",
                        description = "${topCategory.categoryName} is ${String.format("%.0f", topCategory.percentage)}% of your spending. Reducing by 10% could save ৳${String.format("%.0f", potentialSaving)}/month",
                        potentialSavings = potentialSaving,
                        actionRequired = true,
                        priority = 2,
                        icon = "reduce"
                    )
                )
            }

            if (prediction.weeklyTrend == Trend.UP) {
                suggestions.add(
                    SmartSuggestion(
                        type = SuggestionType.OVERSPENDING_WARNING,
                        title = "Spending Trend Increasing",
                        description = "Your spending is trending upward. You're predicted to spend ৳${String.format("%.0f", prediction.predictedWeeklyExpense)} this week.",
                        potentialSavings = null,
                        actionRequired = true,
                        priority = 1,
                        icon = "warning"
                    )
                )
            }

            if (expense > income) {
                suggestions.add(
                    SmartSuggestion(
                        type = SuggestionType.BUDGET_ADJUSTMENT,
                        title = "Expenses Exceed Income",
                        description = "You've spent more than earned this month. Consider reviewing your budget.",
                        potentialSavings = expense - income,
                        actionRequired = true,
                        priority = 1,
                        icon = "alert"
                    )
                )
            }

            if (suggestions.isEmpty()) {
                suggestions.add(
                    SmartSuggestion(
                        type = SuggestionType.SAVING_OPPORTUNITY,
                        title = "Great Financial Health!",
                        description = "You're managing your finances well. Keep up the good work!",
                        potentialSavings = null,
                        actionRequired = false,
                        priority = 5,
                        icon = "celebrate"
                    )
                )
            }

            suggestions.sortedBy { it.priority }
        }
    }

    fun getPersonalBehaviorModel(): Flow<PersonalBehaviorModel> {
        val monthStart = LocalDate.now().withDayOfMonth(1).format(dateFormatter)
        val monthEnd = LocalDate.now().format(dateFormatter)

        return combine(
            transactionDao.getTransactionsBetween(monthStart, monthEnd),
            categoryDao.getAllCategories()
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }

            val daySpending = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { LocalDate.parse(it.date).dayOfWeek.value }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }

            val dayRank = DayOfWeek.entries.associate { day ->
                day.value to (daySpending[day.value] ?: 0.0)
            }

            val categorySpending = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }

            val categoryRank = categorySpending.mapValues { (it.value / (categorySpending.values.sum().takeIf { s -> s > 0 } ?: 1.0)) * 100.0 }

            val avgSize = if (transactions.isNotEmpty()) transactions.map { it.amount }.average() else 0.0

            val overspendingDays = daySpending.filter { it.value > daySpending.values.average() * 1.5 }
            val triggers = overspendingDays.map { (day, amount) ->
                OverspendingTrigger(
                    triggerType = "HIGH_SPENDING_DAY",
                    description = "Tendency to overspend on ${DayOfWeek.of(day).name.lowercase().replaceFirstChar { it.uppercase() }}}",
                    frequency = 1,
                    averageOverspend = amount - daySpending.values.average()
                )
            }

            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val savingScore = if (income > 0) ((income - expense) / income * 100).toInt().coerceIn(0, 100) else 50

            PersonalBehaviorModel(
                daySpendingRank = dayRank,
                hourSpendingRank = emptyMap(),
                categoryPreferences = categoryRank,
                overspendingTriggers = triggers,
                averageTransactionSize = avgSize,
                savingScore = savingScore
            )
        }
    }

    fun getFinancialHealthScore(): Flow<FinancialHealthScore> {
        val monthStart = LocalDate.now().withDayOfMonth(1).format(dateFormatter)
        val monthEnd = LocalDate.now().format(dateFormatter)
        val lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).format(dateFormatter)
        val lastMonthEnd = LocalDate.now().minusMonths(1).format(dateFormatter)

        return combine(
            transactionDao.getMonthlyIncome(monthStart, monthEnd),
            transactionDao.getMonthlyExpense(monthStart, monthEnd),
            transactionDao.getMonthlyExpense(lastMonthStart, lastMonthEnd),
            transactionDao.getMonthlySaved(monthStart, monthEnd)
        ) { income, expense, lastMonthExpense, saved ->
            val savingsScore = if (income > 0) ((saved / income) * 100).toInt().coerceIn(0, 100) else 0

            val spendingControlScore = when {
                expense < income * 0.8 -> 100
                expense < income * 0.9 -> 80
                expense < income -> 60
                expense < income * 1.1 -> 40
                else -> 20
            }

            val trendScore = when {
                lastMonthExpense == 0.0 -> 50
                expense < lastMonthExpense * 0.9 -> 100
                expense < lastMonthExpense -> 70
                expense < lastMonthExpense * 1.1 -> 40
                else -> 20
            }

            val budgetAdherenceScore = if (income > 0) {
                val budget = income * 0.8
                when {
                    expense <= budget -> 100
                    expense <= budget * 1.1 -> 70
                    else -> 30
                }
            } else 50

            val overallScore = (savingsScore + spendingControlScore + trendScore + budgetAdherenceScore) / 4

            val grade = when {
                overallScore >= 90 -> "A+"
                overallScore >= 80 -> "A"
                overallScore >= 70 -> "B"
                overallScore >= 60 -> "C"
                overallScore >= 50 -> "D"
                else -> "F"
            }

            val summary = when {
                overallScore >= 80 -> "Excellent financial health! Keep up the great work."
                overallScore >= 60 -> "Good financial health. Room for improvement."
                overallScore >= 40 -> "Fair financial health. Consider budgeting improvements."
                else -> "Needs attention. Focus on reducing expenses."
            }

            FinancialHealthScore(
                overallScore = overallScore,
                savingsScore = savingsScore,
                spendingControlScore = spendingControlScore,
                budgetAdherenceScore = budgetAdherenceScore,
                trendScore = trendScore,
                grade = grade,
                summary = summary
            )
        }
    }

    fun getStreakInfo(): Flow<StreakInfo> {
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30).format(dateFormatter)

        return transactionDao.getTransactionsBetween(thirtyDaysAgo, today.format(dateFormatter)).map { transactions ->
            val dailyExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.date }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }

            val avgDaily = if (dailyExpenses.isNotEmpty()) dailyExpenses.values.average() else 0.0

            val underBudgetDays = dailyExpenses.count { it.value <= avgDaily * 1.2 }
            val spendingControlDays = dailyExpenses.count { it.value <= avgDaily }
            val savingsStreakDays = dailyExpenses.count { it.value < avgDaily * 0.8 }

            val sortedDays = dailyExpenses.entries.sortedByDescending { it.value }
            val longestUnderBudget = sortedDays.takeWhile { it.value <= avgDaily * 1.2 }.size

            StreakInfo(
                underBudgetDays = underBudgetDays,
                spendingControlDays = spendingControlDays,
                savingsStreakDays = savingsStreakDays,
                longestUnderBudget = longestUnderBudget,
                lastUpdated = today.format(dateFormatter)
            )
        }
    }

    fun getMonthlySummary(month: LocalDate = LocalDate.now()): Flow<MonthlySummary> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)
        val lastMonth = month.minusMonths(1)
        val lastMonthStart = lastMonth.withDayOfMonth(1).format(dateFormatter)
        val lastMonthEnd = lastMonth.format(dateFormatter)

        return combine(
            transactionDao.getMonthlyIncome(monthStart, monthEnd),
            transactionDao.getMonthlyExpense(monthStart, monthEnd),
            transactionDao.getMonthlySaved(monthStart, monthEnd),
            transactionDao.getMonthlyExpense(lastMonthStart, lastMonthEnd)
        ) { income, expense, saved, lastMonthExpense ->
            val savings = income - expense
            val savingsRate = if (income > 0) (savings / income) * 100 else 0.0
            val comparison = if (lastMonthExpense > 0) {
                ((expense - lastMonthExpense) / lastMonthExpense) * 100
            } else 0.0

            MonthlySummary(
                income = income,
                expense = expense,
                savings = savings,
                savingsRate = savingsRate,
                comparisonWithLastMonth = comparison,
                isPositiveTrend = comparison < 0
            )
        }
    }

    fun getDailySummary(date: LocalDate = LocalDate.now()): Flow<DailySummary> {
        val today = date.format(dateFormatter)
        val yesterday = date.minusDays(1).format(dateFormatter)

        return combine(
            transactionDao.getDailyIncome(today),
            transactionDao.getDailyExpense(today),
            transactionDao.getDailyIncome(yesterday),
            transactionDao.getDailyExpense(yesterday)
        ) { todayIncome, todayExpense, yesterdayIncome, yesterdayExpense ->
            val todayNet = todayIncome - todayExpense
            val yesterdayNet = yesterdayIncome - yesterdayExpense
            val comparison = if (yesterdayExpense > 0) {
                ((todayExpense - yesterdayExpense) / yesterdayExpense) * 100
            } else 0.0

            DailySummary(
                date = today,
                income = todayIncome,
                expense = todayExpense,
                netBalance = todayNet,
                comparisonWithYesterday = comparison,
                isPositiveTrend = todayExpense < yesterdayExpense
            )
        }
    }

    fun getWeeklySummary(weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))): Flow<WeeklySummary> {
        val startDate = weekStart.format(dateFormatter)
        val endDate = weekStart.plusDays(6).format(dateFormatter)
        val lastWeekStart = weekStart.minusWeeks(1).format(dateFormatter)
        val lastWeekEnd = weekStart.minusDays(1).format(dateFormatter)

        return combine(
            transactionDao.getTransactionsBetween(startDate, endDate),
            transactionDao.getTransactionsBetween(lastWeekStart, lastWeekEnd)
        ) { thisWeekTransactions, lastWeekTransactions ->
            val thisWeekExpenses = thisWeekTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val lastWeekExpenses = lastWeekTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val comparison = if (lastWeekExpenses > 0) {
                ((thisWeekExpenses - lastWeekExpenses) / lastWeekExpenses) * 100
            } else 0.0

            val dailySummaries = (0..6).map { dayOffset ->
                val day = weekStart.plusDays(dayOffset.toLong())
                val dayStr = day.format(dateFormatter)
                val dayTransactions = thisWeekTransactions.filter { it.date == dayStr }
                val income = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                DailySummary(
                    date = dayStr,
                    income = income,
                    expense = expense,
                    netBalance = income - expense,
                    comparisonWithYesterday = 0.0,
                    isPositiveTrend = true
                )
            }

            WeeklySummary(
                startDate = startDate,
                endDate = endDate,
                totalExpense = thisWeekExpenses,
                averageDailySpending = thisWeekExpenses / 7,
                comparisonWithLastWeek = comparison,
                isPositiveTrend = thisWeekExpenses < lastWeekExpenses,
                days = dailySummaries
            )
        }
    }

    fun getSpendingInsights(month: LocalDate = LocalDate.now()): Flow<List<SpendingInsight>> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)
        val lastMonth = month.minusMonths(1)
        val lastMonthStart = lastMonth.withDayOfMonth(1).format(dateFormatter)
        val lastMonthEnd = lastMonth.format(dateFormatter)

        return combine(
            transactionDao.getExpensesByCategory(monthStart, monthEnd),
            transactionDao.getExpensesByCategory(lastMonthStart, lastMonthEnd),
            categoryDao.getAllCategories()
        ) { thisMonthByCategory, lastMonthByCategory, categories ->
            val categoryMap = categories.associateBy { it.id }
            val totalExpense = thisMonthByCategory.sumOf { it.total }

            thisMonthByCategory.map { categoryTotal ->
                val lastMonthAmount = lastMonthByCategory
                    .find { it.categoryId == categoryTotal.categoryId }?.total ?: 0.0
                val percentage = if (totalExpense > 0) (categoryTotal.total / totalExpense) * 100 else 0.0
                val change = if (lastMonthAmount > 0) {
                    ((categoryTotal.total - lastMonthAmount) / lastMonthAmount) * 100
                } else 0.0

                val category = categoryMap[categoryTotal.categoryId]
                val trend = when {
                    change > 5 -> Trend.UP
                    change < -5 -> Trend.DOWN
                    else -> Trend.STABLE
                }

                val message = when (trend) {
                    Trend.UP -> "Increased by ${String.format("%.1f", change)}% from last month"
                    Trend.DOWN -> "Decreased by ${String.format("%.1f", -change)}% from last month"
                    Trend.STABLE -> "Stable compared to last month"
                }

                SpendingInsight(
                    categoryId = categoryTotal.categoryId,
                    categoryName = category?.name ?: "Unknown",
                    amount = categoryTotal.total,
                    percentage = percentage,
                    trend = trend,
                    message = message
                )
            }.sortedByDescending { it.amount }
        }
    }

    fun getAlerts(month: LocalDate = LocalDate.now()): Flow<List<FinanceAlert>> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)
        val today = LocalDate.now().format(dateFormatter)
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)

        return combine(
            transactionDao.getDailyExpense(today),
            transactionDao.getDailyExpense(yesterday),
            transactionDao.getMonthlyExpense(monthStart, monthEnd),
            transactionDao.getMonthlyIncome(monthStart, monthEnd),
            transactionDao.getTransactionsBetween(monthStart, monthEnd)
        ) { todayExpense, yesterdayExpense, monthExpense, monthIncome, _ ->
            val alerts = mutableListOf<FinanceAlert>()
            val avgDailyExpense = if (monthExpense > 0) monthExpense / LocalDate.now().dayOfMonth else 0.0

            if (todayExpense > avgDailyExpense * 1.5 && avgDailyExpense > 0) {
                alerts.add(
                    FinanceAlert(
                        type = AlertType.OVERSPENDING,
                        message = "You're spending more than usual today! You've spent ৳${String.format("%.0f", todayExpense)} today.",
                        severity = Severity.WARNING,
                        timestamp = today
                    )
                )
            }

            if (todayExpense < yesterdayExpense * 0.5 && yesterdayExpense > 0) {
                alerts.add(
                    FinanceAlert(
                        type = AlertType.LOW_SPENDING,
                        message = "You spent significantly less today. Is this intentional?",
                        severity = Severity.INFO,
                        timestamp = today
                    )
                )
            }

            val savings = monthIncome - monthExpense
            if (savings > 0 && monthIncome > 0) {
                val savingsRate = (savings / monthIncome) * 100
                if (savingsRate > 20) {
                    alerts.add(
                        FinanceAlert(
                            type = AlertType.STREAK_POSITIVE,
                            message = "Great job! You're saving ${String.format("%.1f", savingsRate)}% of your income this month!",
                            severity = Severity.SUCCESS,
                            timestamp = today
                        )
                    )
                }
            }

            val dayOfMonth = LocalDate.now().dayOfMonth
            val daysInMonth = month.lengthOfMonth()
            val daysRemaining = daysInMonth - dayOfMonth
            if (monthIncome > 0 && daysRemaining > 0) {
                val dailyBudget = savings / daysRemaining
                if (dailyBudget < 0) {
                    alerts.add(
                        FinanceAlert(
                            type = AlertType.BUDGET_WARNING,
                            message = "At current rate, you'll exceed your income by end of month.",
                            severity = Severity.WARNING,
                            timestamp = today
                        )
                    )
                }
            }

            alerts
        }
    }

    fun getCategorySpending(month: LocalDate = LocalDate.now()): Flow<List<CategorySpending>> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)

        return combine(
            transactionDao.getExpensesByCategory(monthStart, monthEnd),
            categoryDao.getAllCategories()
        ) { expensesByCategory, categories ->
            val categoryMap = categories.associateBy { it.id }
            val total = expensesByCategory.sumOf { it.total }

            expensesByCategory.map { categoryTotal ->
                CategorySpending(
                    categoryId = categoryTotal.categoryId,
                    categoryName = categoryMap[categoryTotal.categoryId]?.name ?: "Unknown",
                    amount = categoryTotal.total,
                    percentage = if (total > 0) (categoryTotal.total / total) * 100 else 0.0,
                    icon = categoryMap[categoryTotal.categoryId]?.icon ?: "",
                    color = categoryMap[categoryTotal.categoryId]?.color ?: 0
                )
            }.sortedByDescending { it.amount }
        }
    }

    fun getAccountBalances(): Flow<List<com.rudra.lifeledge.core.finance.model.AccountBalance>> {
        return combine(
            accountDao.getAllAccounts(),
            transactionDao.getRecentTransactions(10000)
        ) { accounts, transactions ->
            accounts.map { account ->
                val accountTransactions = transactions.filter { it.accountId == account.id }
                val income = accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val currentBalance = account.balance + income - expense

                com.rudra.lifeledge.core.finance.model.AccountBalance(
                    accountId = account.id,
                    accountName = account.name,
                    openingBalance = account.balance,
                    totalIncome = income,
                    totalExpense = expense,
                    currentBalance = currentBalance,
                    transactionCount = accountTransactions.size
                )
            }
        }
    }

    fun getBehaviorPatterns(month: LocalDate = LocalDate.now()): Flow<List<BehaviorPattern>> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)

        return transactionDao.getTransactionsBetween(monthStart, monthEnd).map { transactions ->
            val dayGroups = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { LocalDate.parse(it.date).dayOfWeek }

            val dayOfWeekNames = mapOf(
                DayOfWeek.MONDAY to "Monday",
                DayOfWeek.TUESDAY to "Tuesday",
                DayOfWeek.WEDNESDAY to "Wednesday",
                DayOfWeek.THURSDAY to "Thursday",
                DayOfWeek.FRIDAY to "Friday",
                DayOfWeek.SATURDAY to "Saturday",
                DayOfWeek.SUNDAY to "Sunday"
            )

            val avgByDay = dayGroups.mapValues { (_, txns) -> txns.sumOf { it.amount } / txns.size }
            val maxSpending = avgByDay.values.maxOrNull() ?: 0.0

            DayOfWeek.entries.map { day ->
                val avg = avgByDay[day] ?: 0.0
                BehaviorPattern(
                    dayOfWeek = dayOfWeekNames[day] ?: day.name,
                    averageSpending = avg,
                    isHighestSpendingDay = avg == maxSpending && avg > 0
                )
            }
        }
    }

    fun getBudgetStatus(monthlyBudget: Double, month: LocalDate = LocalDate.now()): Flow<BudgetStatus> {
        val monthStart = month.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = month.format(dateFormatter)

        return transactionDao.getMonthlyExpense(monthStart, monthEnd).map { spent ->
            val remaining = monthlyBudget - spent
            val percentUsed = if (monthlyBudget > 0) (spent / monthlyBudget) * 100 else 0.0
            val dayOfMonth = month.dayOfMonth
            val daysInMonth = month.lengthOfMonth()
            val daysRemaining = daysInMonth - dayOfMonth

            val projectedOverspend = if (daysRemaining > 0 && spent > 0) {
                val dailyRate = spent / dayOfMonth
                val projected = dailyRate * daysInMonth
                if (projected > monthlyBudget) projected - monthlyBudget else null
            } else null

            BudgetStatus(
                budgetAmount = monthlyBudget,
                spentAmount = spent,
                remainingAmount = remaining,
                percentUsed = percentUsed,
                daysRemainingInMonth = daysRemaining,
                projectedOverspend = projectedOverspend
            )
        }
    }

    fun getFilteredTransactions(
        startDate: String? = null,
        endDate: String? = null,
        type: TransactionType? = null,
        categoryId: Long? = null,
        accountId: Long? = null,
        searchQuery: String? = null
    ): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(1000).map { transactions ->
            transactions.filter { transaction ->
                val matchesDate = (startDate == null || transaction.date >= startDate) &&
                        (endDate == null || transaction.date <= endDate)
                val matchesType = type == null || transaction.type == type
                val matchesCategory = categoryId == null || transaction.categoryId == categoryId
                val matchesAccount = accountId == null || transaction.accountId == accountId
                val matchesSearch = searchQuery.isNullOrBlank() ||
                        (transaction.notes?.contains(searchQuery, ignoreCase = true) == true) ||
                        (transaction.payee?.contains(searchQuery, ignoreCase = true) == true) ||
                        transaction.tags.contains(searchQuery, ignoreCase = true)

                matchesDate && matchesType && matchesCategory && matchesAccount && matchesSearch
            }
        }
    }

    suspend fun deleteTransactionWithBalanceUpdate(transaction: Transaction) {
        deleteTransactionWithIntegrity(transaction)
    }

    suspend fun addTransactionWithBalanceUpdate(transaction: Transaction): Long {
        return insertTransactionWithIntegrity(transaction)
    }

    suspend fun updateTransactionWithBalanceUpdate(transaction: Transaction) {
        val oldTransaction = transactionDao.getTransaction(transaction.id)
        if (oldTransaction != null) {
            updateTransactionWithIntegrity(oldTransaction, transaction)
        }
    }
}
