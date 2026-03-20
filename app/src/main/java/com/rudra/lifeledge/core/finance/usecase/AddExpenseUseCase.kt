package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.AccountRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Use case for adding an expense transaction.
 * Implements double-entry accounting:
 * - Debit: Expense account (category)
 * - Credit: Asset account (payment source)
 */
class AddExpenseUseCase(
    private val transactionEngine: TransactionEngine,
    private val accountRepository: AccountRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Adds an expense transaction.
     * 
     * @param amount The expense amount (must be > 0)
     * @param categoryId The expense category ID
     * @param fromAccountId The account to pay from (default: main balance)
     * @param date The date of the expense (default: today)
     * @param notes Optional notes about the expense
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        amount: Double,
        categoryId: Long,
        fromAccountId: Long? = null,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        // Get or determine the source account
        val sourceAccount = fromAccountId?.let { accountRepository.getAccount(it) }
            ?: accountRepository.getMainBalanceAccount()
        
        // Get or create the expense account for this category
        val expenseAccount = accountRepository.getExpenseAccount(
            categoryName = "Category_$categoryId",
            categoryId = categoryId
        )
        
        // Create the journal entry with double-entry
        return transactionEngine.createJournalEntry(
            date = date,
            description = "Expense: $notes",
            amount = amount,
            type = TransactionType.EXPENSE,
            fromAccountId = sourceAccount.id,
            toAccountId = null, // Expense doesn't have a destination account in traditional sense
            categoryId = expenseAccount.id,
            notes = notes
        )
    }
}
