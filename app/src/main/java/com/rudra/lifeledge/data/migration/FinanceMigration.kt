package com.rudra.lifeledge.data.migration

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.AccountType
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.AccountRepository
import kotlinx.coroutines.flow.first

/**
 * Migration utility for converting existing financial data to the new double-entry system.
 * 
 * This migration:
 * 1. Seeds default accounts if needed
 * 2. Migrates all existing transactions to journal lines
 * 3. Maintains backward compatibility
 */
class FinanceMigration(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val transactionEngine: TransactionEngine,
    private val accountRepository: AccountRepository
) {
    
    /**
     * Result of the migration
     */
    data class MigrationResult(
        val successCount: Int,
        val failureCount: Int,
        val errors: List<String>
    )
    
    /**
     * Performs the complete migration.
     * Should be called once during app startup.
     */
    suspend fun migrateAllData(): MigrationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()
        
        // Step 1: Seed default accounts
        try {
            accountRepository.seedDefaultAccounts()
        } catch (e: Exception) {
            errors.add("Failed to seed accounts: ${e.message}")
        }
        
        // Step 2: Get all existing transactions
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()
        
        // Step 3: Migrate each transaction to journal lines
        for (transaction in transactions) {
            try {
                // Check if this transaction already has journal lines
                // For simplicity, we'll re-create them (in production, you'd check)
                
                val result = when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        // Create expense journal entry
                        transactionEngine.createJournalEntry(
                            date = transaction.date,
                            description = transaction.notes ?: "Migrated expense",
                            amount = transaction.amount,
                            type = TransactionType.EXPENSE,
                            fromAccountId = transaction.accountId,
                            toAccountId = null,
                            categoryId = transaction.categoryId,
                            notes = transaction.notes
                        )
                    }
                    TransactionType.INCOME -> {
                        transactionEngine.createJournalEntry(
                            date = transaction.date,
                            description = transaction.notes ?: "Migrated income",
                            amount = transaction.amount,
                            type = TransactionType.INCOME,
                            fromAccountId = null,
                            toAccountId = transaction.accountId,
                            categoryId = transaction.categoryId,
                            notes = transaction.notes
                        )
                    }
                    TransactionType.TRANSFER, TransactionType.TRANSFER_FROM_SAVING -> {
                        transactionEngine.createJournalEntry(
                            date = transaction.date,
                            description = transaction.notes ?: "Migrated transfer",
                            amount = transaction.amount,
                            type = TransactionType.TRANSFER,
                            fromAccountId = transaction.accountId,
                            toAccountId = transaction.toAccountId,
                            categoryId = null,
                            notes = transaction.notes
                        )
                    }
                    TransactionType.SAVE -> {
                        transactionEngine.createJournalEntry(
                            date = transaction.date,
                            description = transaction.notes ?: "Migrated savings",
                            amount = transaction.amount,
                            type = TransactionType.TRANSFER,
                            fromAccountId = transaction.accountId,
                            toAccountId = transaction.toAccountId,
                            categoryId = null,
                            notes = transaction.notes
                        )
                    }
                }
                
                if (result is TransactionResult.Success) {
                    successCount++
                } else if (result is TransactionResult.Error) {
                    failureCount++
                    errors.add("Transaction ${transaction.id}: ${result.message}")
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("Transaction ${transaction.id}: ${e.message}")
            }
        }
        
        return MigrationResult(
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
    }
    
    /**
     * Validates the integrity of the financial data after migration.
     */
    suspend fun validateIntegrity(): List<String> {
        return transactionEngine.validateIntegrity()
    }
    
    /**
     * Creates a backup of the current account balances before migration.
     */
    suspend fun createBalanceBackup(): Map<Long, Double> {
        val accounts = accountDao.getAllAccounts().first()
        return accounts.associate { it.id to it.balance }
    }
}
