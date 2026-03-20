package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.dao.LoanDao
import com.rudra.lifeledge.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Use case for calculating net worth.
 * 
 * Net Worth = Total Assets - Total Liabilities
 * 
 * This provides a complete picture of financial health.
 */
class GetNetWorthUseCase(
    private val accountDao: AccountDao,
    private val journalLineDao: JournalLineDao,
    private val loanDao: LoanDao
) {
    /**
     * Data class for net worth breakdown
     */
    data class NetWorthBreakdown(
        val totalAssets: Double,
        val totalLiabilities: Double,
        val netWorth: Double,
        val assetAccounts: List<AccountBalance>,
        val liabilityAccounts: List<AccountBalance>
    )
    
    data class AccountBalance(
        val accountId: Long,
        val accountName: String,
        val balance: Double
    )
    
    /**
     * Calculates the complete net worth breakdown.
     * Uses journal lines for accurate balance calculation.
     */
    suspend fun getNetWorth(): NetWorthBreakdown {
        val accounts = accountDao.getAllAccounts().first()
        
        // Separate asset and liability accounts
        val assetAccounts = accounts.filter { 
            it.type in listOf(
                AccountType.CASH,
                AccountType.BANK,
                AccountType.MOBILE_BANKING,
                AccountType.SAVINGS
            )
        }
        
        val liabilityAccounts = accounts.filter { 
            it.type == AccountType.CREDIT_CARD
        }
        
        // Calculate balances from journal lines
        var totalAssets = 0.0
        val assetBalances = mutableListOf<AccountBalance>()
        
        for (account in assetAccounts) {
            val balance = journalLineDao.getAccountBalance(account.id)
            totalAssets += balance
            assetBalances.add(AccountBalance(account.id, account.name, balance))
        }
        
        var totalLiabilities = 0.0
        val liabilityBalances = mutableListOf<AccountBalance>()
        
        for (account in liabilityAccounts) {
            val balance = journalLineDao.getAccountBalance(account.id)
            totalLiabilities += balance
            liabilityBalances.add(AccountBalance(account.id, account.name, balance))
        }
        
        // Add loans as liabilities
        val loans = loanDao.getAllLoans().first()
        for (loan in loans) {
            totalLiabilities += loan.remainingAmount
            liabilityBalances.add(
                AccountBalance(
                    accountId = loan.id,
                    accountName = "Loan: ${loan.name}",
                    balance = loan.remainingAmount
                )
            )
        }
        
        val netWorth = totalAssets - totalLiabilities
        
        return NetWorthBreakdown(
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            assetAccounts = assetBalances,
            liabilityAccounts = liabilityBalances
        )
    }
    
    /**
     * Gets the net worth as a flow for reactive updates.
     */
    fun getNetWorthFlow(): Flow<NetWorthBreakdown> {
        return combine(
            accountDao.getAllAccounts(),
            loanDao.getAllLoans()
        ) { accounts, loans ->
            // This is a simplified version that uses stored balances
            // For production, you'd want to calculate from journal lines
            val assets = accounts.filter { 
                it.type in listOf(
                    AccountType.CASH,
                    AccountType.BANK,
                    AccountType.MOBILE_BANKING,
                    AccountType.SAVINGS
                )
            }
            
            val creditCardAccounts = accounts.filter { 
                it.type == AccountType.CREDIT_CARD
            }
            
            val totalAssets = assets.sumOf { it.balance }
            val totalCreditCardLiabilities = creditCardAccounts.sumOf { it.balance }
            val totalLoanLiabilities = loans.sumOf { it.remainingAmount }
            val totalLiabilities = totalCreditCardLiabilities + totalLoanLiabilities
            
            NetWorthBreakdown(
                totalAssets = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth = totalAssets - totalLiabilities,
                assetAccounts = assets.map { AccountBalance(it.id, it.name, it.balance) },
                liabilityAccounts = creditCardAccounts.map { AccountBalance(it.id, it.name, it.balance) } +
                    loans.map { AccountBalance(it.id, "Loan: ${it.name}", it.remainingAmount) }
            )
        }
    }
    
    /**
     * Gets only the net worth value (simplified).
     */
    suspend fun getNetWorthValue(): Double {
        val breakdown = getNetWorth()
        return breakdown.netWorth
    }
}
