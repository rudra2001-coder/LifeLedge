package com.rudra.lifeledge.core.finance.engine

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.AccountType
import com.rudra.lifeledge.data.local.entity.FinanceAccountType
import com.rudra.lifeledge.data.local.entity.JournalLine
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.local.entity.toFinanceAccountType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Result of a transaction operation.
 * Uses sealed class for type-safe error handling.
 */
sealed class TransactionResult {
    data class Success(val transactionId: Long, val journalEntryId: Long) : TransactionResult()
    data class Error(val message: String, val code: TransactionErrorCode) : TransactionResult()
}

enum class TransactionErrorCode {
    INVALID_AMOUNT,
    ACCOUNTS_NOT_FOUND,
    INSUFFICIENT_BALANCE,
    LOAN_BALANCE_NEGATIVE,
    INVALID_DATE,
    DUPLICATE_TRANSACTION,
    BALANCE_MISMATCH,
    UNKNOWN_ERROR
}

/**
 * TransactionEngine handles the core double-entry accounting logic.
 * It ensures that every transaction maintains the accounting equation:
 * Assets = Liabilities + Equity
 * 
 * Key responsibilities:
 * 1. Validate transactions before processing
 * 2. Create journal entries with proper debit/credit lines
 * 3. Ensure transaction atomicity (all or nothing)
 * 4. Calculate and update account balances
 */
class TransactionEngine(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val journalLineDao: JournalLineDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    companion object {
        // Standard account IDs (these would be seeded in the database)
        const val MAIN_BALANCE_ACCOUNT_ID = 1L
        const val SAVINGS_ACCOUNT_ID = 2L
    }
    
    /**
     * Validates a transaction before processing.
     */
    suspend fun validateTransaction(
        amount: Double,
        fromAccountId: Long?,
        toAccountId: Long?,
        type: TransactionType
    ): TransactionResult {
        // Validate amount > 0
        if (amount <= 0) {
            return TransactionResult.Error(
                "Amount must be greater than zero",
                TransactionErrorCode.INVALID_AMOUNT
            )
        }
        
        // Validate accounts exist
        if (fromAccountId != null && accountDao.getAccount(fromAccountId) == null) {
            return TransactionResult.Error(
                "Source account not found",
                TransactionErrorCode.ACCOUNTS_NOT_FOUND
            )
        }
        
        if (toAccountId != null && accountDao.getAccount(toAccountId) == null) {
            return TransactionResult.Error(
                "Destination account not found",
                TransactionErrorCode.ACCOUNTS_NOT_FOUND
            )
        }
        
        // For transfers and withdrawals, check sufficient balance
        if ((type == TransactionType.TRANSFER || type == TransactionType.TRANSFER_FROM_SAVING) 
            && fromAccountId != null) {
            val balance = journalLineDao.getAccountBalance(fromAccountId)
            if (balance < amount) {
                return TransactionResult.Error(
                    "Insufficient balance. Available: $balance, Required: $amount",
                    TransactionErrorCode.INSUFFICIENT_BALANCE
                )
            }
        }
        
        return TransactionResult.Success(0, 0) // Validation passed
    }
    
    /**
     * Creates a journal entry with proper debit/credit lines for a transaction.
     * This is the core double-entry accounting function.
     */
    suspend fun createJournalEntry(
        date: String,
        description: String,
        amount: Double,
        type: TransactionType,
        fromAccountId: Long?,
        toAccountId: Long?,
        categoryId: Long?,
        notes: String?
    ): TransactionResult {
        // Validate the transaction
        val validationResult = validateTransaction(amount, fromAccountId, toAccountId, type)
        if (validationResult is TransactionResult.Error) {
            return validationResult
        }
        
        // Build journal lines based on transaction type
        val journalLines = buildJournalLines(
            type = type,
            amount = amount,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            categoryId = categoryId
        )
        
        // Verify debits equal credits
        val totalDebits = journalLines.sumOf { it.debit }
        val totalCredits = journalLines.sumOf { it.credit }
        
        if (totalDebits != totalCredits) {
            return TransactionResult.Error(
                "Transaction unbalanced: Debits ($totalDebits) != Credits ($totalCredits)",
                TransactionErrorCode.BALANCE_MISMATCH
            )
        }
        
        try {
            // Create the transaction in the legacy table (for backward compatibility)
            val transaction = Transaction(
                date = date,
                amount = amount,
                type = type,
                categoryId = categoryId ?: 0,
                accountId = fromAccountId ?: toAccountId ?: 0,
                toAccountId = toAccountId,
                notes = notes,
                isRecurring = false,
                recurringId = null,
                cardId = null,
                isCleared = true,
                attachment = null,
                location = null,
                tags = "",
                payee = null
            )
            
            val transactionId = transactionDao.insertTransaction(transaction)
            
            // Create journal lines
            val linesWithEntryId = journalLines.map { it.copy(journalEntryId = transactionId) }
            journalLineDao.insertJournalLines(linesWithEntryId)
            
            return TransactionResult.Success(transactionId, transactionId)
        } catch (e: Exception) {
            return TransactionResult.Error(
                "Failed to create transaction: ${e.message}",
                TransactionErrorCode.UNKNOWN_ERROR
            )
        }
    }
    
    /**
     * Builds journal lines based on transaction type.
     * This implements the double-entry accounting rules:
     * - Expense: Debit Expense account, Credit Asset account
     * - Income: Debit Asset account, Credit Income account
     * - Transfer: Debit destination asset, Credit source asset
     */
    private suspend fun buildJournalLines(
        type: TransactionType,
        amount: Double,
        fromAccountId: Long?,
        toAccountId: Long?,
        categoryId: Long?
    ): List<JournalLine> {
        return when (type) {
            TransactionType.EXPENSE -> {
                // Debit: Expense account (category)
                // Credit: Asset account (payment source)
                val lines = mutableListOf<JournalLine>()
                
                // Debit the expense account
                categoryId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = amount,
                            credit = 0.0,
                            memo = "Expense"
                        )
                    )
                }
                
                // Credit the asset account
                fromAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = 0.0,
                            credit = amount,
                            memo = "Payment"
                        )
                    )
                }
                
                lines
            }
            
            TransactionType.INCOME -> {
                // Debit: Asset account (destination)
                // Credit: Income account (source)
                val lines = mutableListOf<JournalLine>()
                
                // Debit the asset account
                toAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = amount,
                            credit = 0.0,
                            memo = "Receipt"
                        )
                    )
                }
                
                // Credit the income account
                categoryId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = 0.0,
                            credit = amount,
                            memo = "Income"
                        )
                    )
                }
                
                lines
            }
            
            TransactionType.TRANSFER, TransactionType.TRANSFER_FROM_SAVING -> {
                // Debit: Destination account
                // Credit: Source account
                val lines = mutableListOf<JournalLine>()
                
                // Debit destination
                toAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = amount,
                            credit = 0.0,
                            memo = "Transfer in"
                        )
                    )
                }
                
                // Credit source
                fromAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = 0.0,
                            credit = amount,
                            memo = "Transfer out"
                        )
                    )
                }
                
                lines
            }
            
            TransactionType.SAVE -> {
                // Similar to transfer but to savings
                val lines = mutableListOf<JournalLine>()
                
                // Debit savings account
                toAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = amount,
                            credit = 0.0,
                            memo = "Savings deposit"
                        )
                    )
                }
                
                // Credit source account
                fromAccountId?.let {
                    lines.add(
                        JournalLine(
                            journalEntryId = 0,
                            accountId = it,
                            debit = 0.0,
                            credit = amount,
                            memo = "Savings withdrawal"
                        )
                    )
                }
                
                lines
            }
        }
    }
    
    /**
     * Reverses a transaction by creating opposite journal entries.
     */
    suspend fun reverseTransaction(transactionId: Long): TransactionResult {
        val transaction = transactionDao.getTransaction(transactionId)
            ?: return TransactionResult.Error(
                "Transaction not found",
                TransactionErrorCode.UNKNOWN_ERROR
            )
        
        return createJournalEntry(
            date = LocalDate.now().format(dateFormatter),
            description = "Reversal: ${transaction.notes ?: ""}",
            amount = transaction.amount,
            type = when (transaction.type) {
                TransactionType.EXPENSE -> TransactionType.INCOME
                TransactionType.INCOME -> TransactionType.EXPENSE
                TransactionType.TRANSFER -> TransactionType.TRANSFER
                TransactionType.TRANSFER_FROM_SAVING -> TransactionType.TRANSFER_FROM_SAVING
                TransactionType.SAVE -> TransactionType.TRANSFER_FROM_SAVING
            },
            fromAccountId = transaction.toAccountId,
            toAccountId = transaction.accountId,
            categoryId = transaction.categoryId,
            notes = "Reversed transaction #$transactionId"
        )
    }
    
    /**
     * Calculates the balance for an account from journal lines.
     * This is the preferred method as it doesn't trust stored balances.
     */
    suspend fun calculateAccountBalance(accountId: Long): Double {
        return journalLineDao.getAccountBalance(accountId)
    }
    
    /**
     * Gets the balance flow for reactive updates.
     */
    fun getAccountBalanceFlow(accountId: Long) = journalLineDao.getAccountBalanceFlow(accountId)
    
    /**
     * Validates that the database maintains integrity.
     * Checks that all journal entries are balanced.
     */
    suspend fun validateIntegrity(): List<String> {
        val errors = mutableListOf<String>()
        
        // Get all transactions
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()
        
        for (transaction in transactions) {
            val isBalanced = journalLineDao.isBalanced(transaction.id)
            if (!isBalanced) {
                errors.add("Transaction ${transaction.id} is not balanced")
            }
        }
        
        return errors
    }
}
