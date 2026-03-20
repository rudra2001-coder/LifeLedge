package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.entity.JournalLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Use case for getting balance history over time.
 * Provides data points for charts and trend analysis.
 */
class GetBalanceHistoryUseCase(
    private val journalLineDao: JournalLineDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Data point for balance history chart
     */
    data class BalancePoint(
        val date: String,
        val balance: Double,
        val debit: Double,
        val credit: Double
    )
    
    /**
     * Gets balance history for an account between dates.
     * 
     * @param accountId The account ID
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     * @return List of balance points
     */
    suspend operator fun invoke(
        accountId: Long,
        startDate: String,
        endDate: String
    ): List<BalancePoint> {
        val lines = journalLineDao.getLinesForAccountInRange(accountId, startDate, endDate)
            .let { flow ->
                // Collect the flow
                var result = emptyList<JournalLine>()
                flow.collect { result = it }
                result
            }
        
        // Group by date and calculate cumulative balance
        return calculateBalancePoints(lines)
    }
    
    /**
     * Gets monthly balance history for an account.
     * 
     * @param accountId The account ID
     * @param months Number of months to look back
     * @return List of monthly balance points
     */
    suspend fun getMonthlyHistory(accountId: Long, months: Int): List<BalancePoint> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(months.toLong())
        
        return invoke(
            accountId = accountId,
            startDate = startDate.format(dateFormatter),
            endDate = endDate.format(dateFormatter)
        )
    }
    
    /**
     * Gets weekly balance history for an account.
     * 
     * @param accountId The account ID
     * @param weeks Number of weeks to look back
     * @return List of weekly balance points
     */
    suspend fun getWeeklyHistory(accountId: Long, weeks: Int): List<BalancePoint> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusWeeks(weeks.toLong())
        
        return invoke(
            accountId = accountId,
            startDate = startDate.format(dateFormatter),
            endDate = endDate.format(dateFormatter)
        )
    }
    
    /**
     * Gets daily balance history for an account.
     * 
     * @param accountId The account ID
     * @param days Number of days to look back
     * @return List of daily balance points
     */
    suspend fun getDailyHistory(accountId: Long, days: Int): List<BalancePoint> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        return invoke(
            accountId = accountId,
            startDate = startDate.format(dateFormatter),
            endDate = endDate.format(dateFormatter)
        )
    }
    
    /**
     * Gets balance history as a Flow for reactive updates.
     */
    fun getBalanceHistoryFlow(
        accountId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<BalancePoint>> {
        return journalLineDao.getLinesForAccountInRange(accountId, startDate, endDate)
            .map { lines -> calculateBalancePoints(lines) }
    }
    
    /**
     * Calculates balance points from journal lines.
     * Groups by date and calculates cumulative balance.
     */
    private fun calculateBalancePoints(lines: List<JournalLine>): List<BalancePoint> {
        // Sort lines by date (we'd need to join with journal_entries for proper date sorting)
        // For now, we'll calculate cumulative balance in order of lines
        
        var runningBalance = 0.0
        val dailyTotals = mutableMapOf<String, Pair<Double, Double>>() // date -> (debit, credit)
        
        for (line in lines) {
            val date = "unknown" // We'd get this from the journal entry
            val current = dailyTotals[date] ?: (0.0 to 0.0)
            dailyTotals[date] = (current.first + line.debit) to (current.second + line.credit)
        }
        
        return dailyTotals.map { (date, amounts) ->
            val (debit, credit) = amounts
            runningBalance += debit - credit
            BalancePoint(
                date = date,
                balance = runningBalance,
                debit = debit,
                credit = credit
            )
        }.sortedBy { it.date }
    }
    
    /**
     * Gets the minimum balance in a period.
     */
    suspend fun getMinimumBalance(
        accountId: Long,
        startDate: String,
        endDate: String
    ): Double {
        val history = invoke(accountId, startDate, endDate)
        return history.minOfOrNull { it.balance } ?: 0.0
    }
    
    /**
     * Gets the maximum balance in a period.
     */
    suspend fun getMaximumBalance(
        accountId: Long,
        startDate: String,
        endDate: String
    ): Double {
        val history = invoke(accountId, startDate, endDate)
        return history.maxOfOrNull { it.balance } ?: 0.0
    }
    
    /**
     * Gets the average balance in a period.
     */
    suspend fun getAverageBalance(
        accountId: Long,
        startDate: String,
        endDate: String
    ): Double {
        val history = invoke(accountId, startDate, endDate)
        return if (history.isNotEmpty()) {
            history.map { it.balance }.average()
        } else {
            0.0
        }
    }
}
