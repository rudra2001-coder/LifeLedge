package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.AccountRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Use case for transferring money between accounts.
 * Implements double-entry accounting:
 * - Debit: Destination account
 * - Credit: Source account
 */
class TransferUseCase(
    private val transactionEngine: TransactionEngine,
    private val accountRepository: AccountRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Transfer types supported by the app
     */
    enum class TransferType {
        MAIN_TO_SAVINGS,
        SAVINGS_TO_MAIN,
        BANK_TO_MAIN,
        MAIN_TO_BANK,
        ACCOUNT_TO_ACCOUNT
    }
    
    /**
     * Transfers money between accounts.
     * 
     * @param amount The amount to transfer (must be > 0)
     * @param fromAccountId The source account ID
     * @param toAccountId The destination account ID
     * @param transferType The type of transfer
     * @param date The date of the transfer (default: today)
     * @param notes Optional notes about the transfer
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        amount: Double,
        fromAccountId: Long,
        toAccountId: Long,
        transferType: TransferType = TransferType.ACCOUNT_TO_ACCOUNT,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        // Validate sufficient balance
        val validationResult = transactionEngine.validateTransaction(
            amount = amount,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            type = TransactionType.TRANSFER
        )
        
        if (validationResult is TransactionResult.Error) {
            return validationResult
        }
        
        // Create the journal entry with double-entry
        val description = when (transferType) {
            TransferType.MAIN_TO_SAVINGS -> "Transfer to Savings"
            TransferType.SAVINGS_TO_MAIN -> "Transfer from Savings"
            TransferType.BANK_TO_MAIN -> "Bank to Main"
            TransferType.MAIN_TO_BANK -> "Main to Bank"
            TransferType.ACCOUNT_TO_ACCOUNT -> notes ?: "Transfer"
        }
        
        return transactionEngine.createJournalEntry(
            date = date,
            description = description,
            amount = amount,
            type = TransactionType.TRANSFER,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            categoryId = null,
            notes = notes
        )
    }
    
    /**
     * Convenience method to transfer from main balance to savings.
     */
    suspend fun transferToSavings(
        amount: Double,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        val mainAccount = accountRepository.getMainBalanceAccount()
        val savingsAccount = accountRepository.getSavingsAccount()
        
        return invoke(
            amount = amount,
            fromAccountId = mainAccount.id,
            toAccountId = savingsAccount.id,
            transferType = TransferType.MAIN_TO_SAVINGS,
            date = date,
            notes = notes
        )
    }
    
    /**
     * Convenience method to transfer from savings to main balance.
     */
    suspend fun transferFromSavings(
        amount: Double,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        val mainAccount = accountRepository.getMainBalanceAccount()
        val savingsAccount = accountRepository.getSavingsAccount()
        
        return invoke(
            amount = amount,
            fromAccountId = savingsAccount.id,
            toAccountId = mainAccount.id,
            transferType = TransferType.SAVINGS_TO_MAIN,
            date = date,
            notes = notes
        )
    }
    
    /**
     * Convenience method to transfer from bank to main balance.
     */
    suspend fun bankToMain(
        amount: Double,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        val mainAccount = accountRepository.getMainBalanceAccount()
        val bankAccount = accountRepository.getBankAccount()
        
        return invoke(
            amount = amount,
            fromAccountId = bankAccount.id,
            toAccountId = mainAccount.id,
            transferType = TransferType.BANK_TO_MAIN,
            date = date,
            notes = notes
        )
    }
    
    /**
     * Convenience method to transfer from main balance to bank.
     */
    suspend fun mainToBank(
        amount: Double,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        val mainAccount = accountRepository.getMainBalanceAccount()
        val bankAccount = accountRepository.getBankAccount()
        
        return invoke(
            amount = amount,
            fromAccountId = mainAccount.id,
            toAccountId = bankAccount.id,
            transferType = TransferType.MAIN_TO_BANK,
            date = date,
            notes = notes
        )
    }
}
