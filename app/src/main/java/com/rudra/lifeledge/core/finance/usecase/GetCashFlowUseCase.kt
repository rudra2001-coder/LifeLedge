package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Use case for getting cash flow reports (income vs expense).
 * Provides period-based analysis of money flow.
 */
class GetCashFlowUseCase(
    private val transactionDao: TransactionDao,
    private val journalLineDao: JournalLineDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Data class for cash flow summary
     */
    data class CashFlowSummary(
        val period: String,
        val totalIncome: Double,
        val totalExpense: Double,
        val netCashFlow: Double,
        val savingsRate: Double
    )
    
    /**
     * Data class for cash flow by category
     */
    data class CategoryCashFlow(
        val categoryId: Long,
        val categoryName: String,
        val amount: Double,
        val percentage: Double
    )
    
    /**
     * Gets cash flow for the current month.
     */
    suspend fun getCurrentMonthCashFlow(): CashFlowSummary {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).format(dateFormatter)
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).format(dateFormatter)
        
        return getCashFlowForPeriod(startOfMonth, endOfMonth)
    }
    
    /**
     * Gets cash flow for a specific month.
     */
    suspend fun getMonthlyCashFlow(yearMonth: YearMonth): CashFlowSummary {
        val startOfMonth = yearMonth.atDay(1).format(dateFormatter)
        val endOfMonth = yearMonth.atEndOfMonth().format(dateFormatter)
        
        return getCashFlowForPeriod(startOfMonth, endOfMonth)
    }
    
    /**
     * Gets cash flow for a date range.
     */
    suspend fun getCashFlowForPeriod(startDate: String, endDate: String): CashFlowSummary {
        val totalIncome = transactionDao.getTotalByType(TransactionType.INCOME, startDate, endDate) ?: 0.0
        val totalExpense = transactionDao.getTotalByType(TransactionType.EXPENSE, startDate, endDate) ?: 0.0
        
        val netCashFlow = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) (netCashFlow / totalIncome) * 100 else 0.0
        
        return CashFlowSummary(
            period = "$startDate to $endDate",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate
        )
    }
    
    /**
     * Gets weekly cash flow.
     */
    suspend fun getWeeklyCashFlow(weekStartDate: LocalDate): CashFlowSummary {
        val startDate = weekStartDate.format(dateFormatter)
        val endDate = weekStartDate.plusDays(6).format(dateFormatter)
        
        return getCashFlowForPeriod(startDate, endDate)
    }
    
    /**
     * Gets daily cash flow for today.
     */
    suspend fun getTodayCashFlow(): CashFlowSummary {
        val today = LocalDate.now().format(dateFormatter)
        return getCashFlowForPeriod(today, today)
    }
    
    /**
     * Gets expense breakdown by category.
     */
    suspend fun getExpensesByCategory(startDate: String, endDate: String): List<CategoryCashFlow> {
        val categoryTotals = transactionDao.getExpensesByCategory(startDate, endDate).first()
        val totalExpense = categoryTotals.sumOf { it.total }
        
        return categoryTotals.map { ct ->
            CategoryCashFlow(
                categoryId = ct.categoryId,
                categoryName = "Category ${ct.categoryId}",
                amount = ct.total,
                percentage = if (totalExpense > 0) (ct.total / totalExpense) * 100 else 0.0
            )
        }
    }
    
    /**
     * Gets income breakdown by source.
     */
    suspend fun getIncomeBySource(startDate: String, endDate: String): List<CategoryCashFlow> {
        // This would need a similar query to getExpensesByCategory
        // For now, returning empty list as placeholder
        val totalIncome = transactionDao.getTotalByType(TransactionType.INCOME, startDate, endDate) ?: 0.0
        
        return listOf(
            CategoryCashFlow(
                categoryId = 0,
                categoryName = "Total Income",
                amount = totalIncome,
                percentage = if (totalIncome > 0) 100.0 else 0.0
            )
        )
    }
    
    /**
     * Gets cash flow trend for the last N months.
     */
    suspend fun getCashFlowTrend(months: Int): List<CashFlowSummary> {
        val trend = mutableListOf<CashFlowSummary>()
        val now = YearMonth.now()
        
        for (i in 0 until months) {
            val yearMonth = now.minusMonths(i.toLong())
            val summary = getMonthlyCashFlow(yearMonth)
            trend.add(summary)
        }
        
        return trend.reversed()
    }
    
    /**
     * Gets a flow of current month's cash flow for reactive updates.
     */
    fun getCurrentMonthCashFlowFlow(): Flow<CashFlowSummary> {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).format(dateFormatter)
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).format(dateFormatter)
        
        return transactionDao.getMonthlyIncome(startOfMonth, endOfMonth).let { incomeFlow ->
            kotlinx.coroutines.flow.combine(
                incomeFlow,
                transactionDao.getMonthlyExpense(startOfMonth, endOfMonth)
            ) { income, expense ->
                val netCashFlow = income - expense
                val savingsRate = if (income > 0) (netCashFlow / income) * 100 else 0.0
                
                CashFlowSummary(
                    period = "$startOfMonth to $endOfMonth",
                    totalIncome = income,
                    totalExpense = expense,
                    netCashFlow = netCashFlow,
                    savingsRate = savingsRate
                )
            }
        }
    }
}
