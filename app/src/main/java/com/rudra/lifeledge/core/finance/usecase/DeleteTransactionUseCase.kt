package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.dao.TransactionDao

/**
 * Use case for deleting a transaction with proper reversal.
 * Implements double-entry reversal:
 * - Creates new journal entries that reverse the original transaction
 * - Original transaction remains in the system for audit trail
 */
class DeleteTransactionUseCase(
    private val transactionDao: TransactionDao,
    private val transactionEngine: TransactionEngine
) {
    /**
     * Deletes a transaction by creating a reversal entry.
     * 
     * @param transactionId The ID of the transaction to delete
     * @param notes Optional notes about why the transaction was deleted
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        transactionId: Long,
        notes: String? = null
    ): TransactionResult {
        // Get the original transaction
        val originalTransaction = transactionDao.getTransaction(transactionId)
            ?: return TransactionResult.Error(
                "Transaction not found",
                com.rudra.lifeledge.core.finance.engine.TransactionErrorCode.UNKNOWN_ERROR
            )
        
        // Create a reversal transaction
        // This essentially swaps the debit and credit entries
        return transactionEngine.reverseTransaction(transactionId)
    }
    
    /**
     * Permanently deletes a transaction without reversal.
     * Use with caution - this breaks the audit trail.
     * 
     * @param transactionId The ID of the transaction to permanently delete
     * @return Result with success or failure
     */
    suspend fun permanentDelete(transactionId: Long): Result<Unit> {
        return try {
            val transaction = transactionDao.getTransaction(transactionId)
                ?: return Result.failure(Exception("Transaction not found"))
            
            transactionDao.deleteTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
