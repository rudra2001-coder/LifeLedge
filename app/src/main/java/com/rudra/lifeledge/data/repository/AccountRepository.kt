package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.AccountType
import com.rudra.lifeledge.data.local.entity.FinanceAccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for account operations with support for double-entry accounting.
 * Provides methods for getting or creating standard accounts.
 */
class AccountRepository(
    private val accountDao: AccountDao,
    private val journalLineDao: JournalLineDao
) {
    companion object {
        // Standard account IDs
        const val MAIN_BALANCE_ID = 1L
        const val SAVINGS_ID = 2L
        const val CASH_ID = 3L
        const val BANK_ID = 4L
        
        // Standard expense category IDs (these would be categories, not accounts)
        const val FOOD_EXPENSE_ID = 1L
        const val TRANSPORT_EXPENSE_ID = 2L
        const val UTILITIES_EXPENSE_ID = 3L
        const val ENTERTAINMENT_EXPENSE_ID = 4L
        const val SHOPPING_EXPENSE_ID = 5L
        
        // Standard income category IDs
        const val SALARY_INCOME_ID = 1L
        const val FREELANCE_INCOME_ID = 2L
        const val INVESTMENT_INCOME_ID = 3L
        const val OTHER_INCOME_ID = 4L
    }
    
    /**
     * Gets all active accounts.
     */
    fun getActiveAccounts(): Flow<List<Account>> = accountDao.getActiveAccounts()
    
    /**
     * Gets all accounts.
     */
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()
    
    /**
     * Gets the total balance across all active accounts.
     */
    fun getTotalBalance(): Flow<Double?> = accountDao.getTotalBalance()
    
    /**
     * Gets a specific account by ID.
     */
    suspend fun getAccount(id: Long): Account? = accountDao.getAccount(id)
    
    /**
     * Saves or updates an account.
     */
    suspend fun saveAccount(account: Account): Long = accountDao.insertAccount(account)
    
    /**
     * Gets or creates the main balance account (cash on hand).
     * This is the primary account for daily transactions.
     */
    suspend fun getMainBalanceAccount(): Account {
        return accountDao.getAccount(MAIN_BALANCE_ID) ?: run {
            accountDao.insertAccount(
                Account(
                    id = MAIN_BALANCE_ID,
                    name = "Main Balance",
                    type = AccountType.CASH,
                    balance = 0.0,
                    currency = "BDT",
                    icon = "wallet",
                    isActive = true,
                    notes = "Primary cash account"
                )
            )
            accountDao.getAccount(MAIN_BALANCE_ID)!!
        }
    }
    
    /**
     * Gets or creates the savings account.
     */
    suspend fun getSavingsAccount(): Account {
        return accountDao.getAccount(SAVINGS_ID) ?: run {
            accountDao.insertAccount(
                Account(
                    id = SAVINGS_ID,
                    name = "Savings",
                    type = AccountType.SAVINGS,
                    balance = 0.0,
                    currency = "BDT",
                    icon = "savings",
                    isActive = true,
                    notes = "Savings account"
                )
            )
            accountDao.getAccount(SAVINGS_ID)!!
        }
    }
    
    /**
     * Gets or creates a bank account.
     */
    suspend fun getBankAccount(): Account {
        return accountDao.getAccount(BANK_ID) ?: run {
            accountDao.insertAccount(
                Account(
                    id = BANK_ID,
                    name = "Bank Account",
                    type = AccountType.BANK,
                    balance = 0.0,
                    currency = "BDT",
                    icon = "account_balance",
                    isActive = true,
                    notes = "Primary bank account"
                )
            )
            accountDao.getAccount(BANK_ID)!!
        }
    }
    
    /**
     * Gets or creates a loan account for a specific loan.
     * Loan accounts track the remaining balance of a loan.
     */
    suspend fun getLoanAccount(loanId: Long): Account {
        val loanAccountId = 1000 + loanId // Offset to avoid conflicts
        return accountDao.getAccount(loanAccountId) ?: run {
            accountDao.insertAccount(
                Account(
                    id = loanAccountId,
                    name = "Loan #$loanId",
                    type = AccountType.CREDIT_CARD, // Treated as liability
                    balance = 0.0,
                    currency = "BDT",
                    icon = "credit_card",
                    isActive = true,
                    notes = "Loan account for loan #$loanId"
                )
            )
            accountDao.getAccount(loanAccountId)!!
        }
    }
    
    /**
     * Gets or creates an expense account for a category.
     * In double-entry accounting, expenses are tracked as accounts.
     */
    suspend fun getExpenseAccount(categoryName: String, categoryId: Long): Account {
        // Check if we already have an account for this expense category
        val existingAccounts = accountDao.getAccountsByType(AccountType.CASH).first()
        val existing = existingAccounts.find { it.name == "Expense: $categoryName" }
        
        if (existing != null) return existing
        
        // Create a new expense account
        val expenseAccountId = 2000 + categoryId // Offset for expense accounts
        return accountDao.getAccount(expenseAccountId) ?: run {
            accountDao.insertAccount(
                Account(
                    id = expenseAccountId,
                    name = "Expense: $categoryName",
                    type = AccountType.CASH, // Will be treated as expense in journal
                    balance = 0.0,
                    currency = "BDT",
                    icon = "category",
                    isActive = true,
                    notes = "Expense category: $categoryName"
                )
            )
            accountDao.getAccount(expenseAccountId)!!
        }
    }
    
    /**
     * Gets or creates an income account for a source.
     * In double-entry accounting, income sources are tracked as accounts.
     */
    suspend fun getIncomeAccount(sourceName: String, sourceId: Long): Account {
        // Check if we already have an account for this income source
        val existingAccounts = accountDao.getAccountsByType(AccountType.BANK).first()
        val existing = existingAccounts.find { it.name == "Income: $sourceName" }
        
        if (existing != null) return existing
        
        // Create a new income account
        val incomeAccountId = 3000 + sourceId // Offset for income accounts
        return accountDao.getAccount(incomeAccountId) ?: run {
            accountDao.insertAccount(
                Account(
                    id = incomeAccountId,
                    name = "Income: $sourceName",
                    type = AccountType.BANK, // Will be treated as income in journal
                    balance = 0.0,
                    currency = "BDT",
                    icon = "attach_money",
                    isActive = true,
                    notes = "Income source: $sourceName"
                )
            )
            accountDao.getAccount(incomeAccountId)!!
        }
    }
    
    /**
     * Gets accounts by type.
     */
    fun getAccountsByType(type: AccountType): Flow<List<Account>> = 
        accountDao.getAccountsByType(type)
    
    /**
     * Calculates the balance for an account from journal lines.
     * This is the preferred method as it doesn't trust stored balances.
     */
    suspend fun getCalculatedBalance(accountId: Long): Double {
        return journalLineDao.getAccountBalance(accountId)
    }
    
    /**
     * Gets the balance flow for reactive updates.
     */
    fun getBalanceFlow(accountId: Long): Flow<Double> {
        return journalLineDao.getAccountBalanceFlow(accountId)
    }
    
    /**
     * Gets total balance across multiple accounts.
     */
    suspend fun getTotalBalanceFromAccounts(accountIds: List<Long>): Double {
        var total = 0.0
        for (id in accountIds) {
            total += journalLineDao.getAccountBalance(id)
        }
        return total
    }
    
    /**
     * Seeds the database with default accounts if they don't exist.
     */
    suspend fun seedDefaultAccounts() {
        // Ensure main balance account exists
        getMainBalanceAccount()
        
        // Ensure savings account exists
        getSavingsAccount()
        
        // Ensure bank account exists
        getBankAccount()
    }
}
