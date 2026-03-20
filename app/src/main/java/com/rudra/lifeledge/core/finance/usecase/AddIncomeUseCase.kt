package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.AccountRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Use case for adding an income transaction.
 * Implements double-entry accounting:
 * - Debit: Asset account (destination)
 * - Credit: Income account (source)
 */
class AddIncomeUseCase(
    private val transactionEngine: TransactionEngine,
    private val accountRepository: AccountRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Adds an income transaction.
     * 
     * @param amount The income amount (must be > 0)
     * @param sourceId The income source ID (salary, freelance, etc.)
     * @param sourceName The name of the income source
     * @param toAccountId The account to receive money (default: main balance)
     * @param date The date of the income (default: today)
     * @param notes Optional notes about the income
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        amount: Double,
        sourceId: Long,
        sourceName: String,
        toAccountId: Long? = null,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        // Get or determine the destination account
        val destinationAccount = toAccountId?.let { accountRepository.getAccount(it) }
            ?: accountRepository.getMainBalanceAccount()
        
        // Get or create the income account for this source
        val incomeAccount = accountRepository.getIncomeAccount(
            sourceName = sourceName,
            sourceId = sourceId
        )
        
        // Create the journal entry with double-entry
        return transactionEngine.createJournalEntry(
            date = date,
            description = "Income: $sourceName - $notes",
            amount = amount,
            type = TransactionType.INCOME,
            fromAccountId = null, // Income doesn't have a source account in traditional sense
            toAccountId = destinationAccount.id,
            categoryId = incomeAccount.id,
            notes = notes
        )
    }
}
