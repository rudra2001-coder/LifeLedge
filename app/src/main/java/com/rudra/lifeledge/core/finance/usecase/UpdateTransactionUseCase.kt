package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType

/**
 * Use case for updating a transaction.
 * Implements update by first reversing the old transaction, then creating a new one.
 */
class UpdateTransactionUseCase(
    private val transactionEngine: TransactionEngine
) {
    /**
     * Data class for transaction update data
     */
    data class TransactionUpdate(
        val amount: Double,
        val date: String,
        val type: TransactionType,
        val fromAccountId: Long?,
        val toAccountId: Long?,
        val categoryId: Long?,
        val notes: String?
    )
    
    /**
     * Updates a transaction by reversing the old one and creating a new one.
     * This maintains the audit trail.
     * 
     * @param oldTransactionId The ID of the transaction to update
     * @param newData The new transaction data
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        oldTransactionId: Long,
        newData: TransactionUpdate
    ): TransactionResult {
        // Reverse the old transaction
        val reversalResult = transactionEngine.reverseTransaction(oldTransactionId)
        
        if (reversalResult is TransactionResult.Error) {
            return reversalResult
        }
        
        // Create the new transaction
        return transactionEngine.createJournalEntry(
            date = newData.date,
            description = newData.notes ?: "Updated transaction",
            amount = newData.amount,
            type = newData.type,
            fromAccountId = newData.fromAccountId,
            toAccountId = newData.toAccountId,
            categoryId = newData.categoryId,
            notes = newData.notes
        )
    }
    
    /**
     * Updates only the amount of a transaction.
     */
    suspend fun updateAmount(
        transactionId: Long,
        newAmount: Double,
        originalTransaction: Transaction
    ): TransactionResult {
        return invoke(
            oldTransactionId = transactionId,
            newData = TransactionUpdate(
                amount = newAmount,
                date = originalTransaction.date,
                type = originalTransaction.type,
                fromAccountId = originalTransaction.accountId,
                toAccountId = originalTransaction.toAccountId,
                categoryId = originalTransaction.categoryId,
                notes = originalTransaction.notes
            )
        )
    }
    
    /**
     * Updates only the date of a transaction.
     */
    suspend fun updateDate(
        transactionId: Long,
        newDate: String,
        originalTransaction: Transaction
    ): TransactionResult {
        return invoke(
            oldTransactionId = transactionId,
            newData = TransactionUpdate(
                amount = originalTransaction.amount,
                date = newDate,
                type = originalTransaction.type,
                fromAccountId = originalTransaction.accountId,
                toAccountId = originalTransaction.toAccountId,
                categoryId = originalTransaction.categoryId,
                notes = originalTransaction.notes
            )
        )
    }
    
    /**
     * Updates only the category of a transaction.
     */
    suspend fun updateCategory(
        transactionId: Long,
        newCategoryId: Long,
        originalTransaction: Transaction
    ): TransactionResult {
        return invoke(
            oldTransactionId = transactionId,
            newData = TransactionUpdate(
                amount = originalTransaction.amount,
                date = originalTransaction.date,
                type = originalTransaction.type,
                fromAccountId = originalTransaction.accountId,
                toAccountId = originalTransaction.toAccountId,
                categoryId = newCategoryId,
                notes = originalTransaction.notes
            )
        )
    }
}
