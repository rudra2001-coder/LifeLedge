package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.JournalLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Use case for getting account balances.
 * Provides methods to calculate balances from journal lines (the preferred method)
 * rather than trusting stored balances.
 */
class GetBalanceUseCase(
    private val accountDao: AccountDao,
    private val journalLineDao: JournalLineDao
) {
    /**
     * Gets the balance for a specific account calculated from journal lines.
     * This is the preferred method as it doesn't trust stored balances.
     */
    suspend fun getAccountBalance(accountId: Long): Double {
        return journalLineDao.getAccountBalance(accountId)
    }
    
    /**
     * Gets the balance flow for reactive updates.
     */
    fun getAccountBalanceFlow(accountId: Long): Flow<Double> {
        return journalLineDao.getAccountBalanceFlow(accountId)
    }
    
    /**
     * Gets balances for multiple accounts.
     */
    fun getAccountBalancesFlow(accountIds: List<Long>): Flow<Map<Long, Double>> {
        return journalLineDao.getAccountBalances(accountIds).map { balances ->
            balances.associate { it.accountId to it.balance }
        }
    }
    
    /**
     * Gets the total balance across all active accounts.
     * Calculates from journal lines for accuracy.
     */
    suspend fun getTotalBalance(): Double {
        val accounts = accountDao.getActiveAccounts().first()
        var total = 0.0
        for (account in accounts) {
            total += journalLineDao.getAccountBalance(account.id)
        }
        return total
    }
    
    /**
     * Gets the total balance flow across all active accounts.
     */
    fun getTotalBalanceFlow(): Flow<Double> {
        return accountDao.getActiveAccounts().map { accounts ->
            // This is a simplified calculation - in production you'd want to 
            // aggregate from journal lines
            accounts.sumOf { it.balance }
        }
    }
    
    /**
     * Gets the balance history for an account between dates.
     */
    fun getAccountBalanceHistory(
        accountId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<JournalLine>> {
        return journalLineDao.getLinesForAccountInRange(accountId, startDate, endDate)
            .map { lines ->
                lines.sortedByDescending { it.id }
            }
    }
}
