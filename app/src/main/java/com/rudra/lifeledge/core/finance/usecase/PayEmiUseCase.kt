package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.dao.EMIPaymentDao
import com.rudra.lifeledge.data.local.dao.LoanDao
import com.rudra.lifeledge.data.local.entity.EMIPayment
import com.rudra.lifeledge.data.local.entity.Loan
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.repository.AccountRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Use case for paying EMI on a loan.
 * Implements double-entry accounting:
 * - Debit: Loan Liability account (reduces the loan balance)
 * - Credit: Asset account (payment source)
 * 
 * The EMI is split into:
 * - Principal portion: Reduces the loan balance
 * - Interest portion: Is recorded as expense
 */
class PayEmiUseCase(
    private val transactionEngine: TransactionEngine,
    private val accountRepository: AccountRepository,
    private val loanDao: LoanDao,
    private val emiPaymentDao: EMIPaymentDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Pays an EMI installment for a loan.
     * 
     * @param loanId The loan ID
     * @param paymentAmount The amount being paid (should match EMI, but can be more for prepayment)
     * @param fromAccountId The account to pay from (default: main balance)
     * @param date The date of payment (default: today)
     * @param notes Optional notes
     * @return TransactionResult with success or error information
     */
    suspend operator fun invoke(
        loanId: Long,
        paymentAmount: Double? = null,
        fromAccountId: Long? = null,
        date: String = LocalDate.now().format(dateFormatter),
        notes: String? = null
    ): TransactionResult {
        // Get the loan
        val loan = loanDao.getLoan(loanId)
            ?: return TransactionResult.Error("Loan not found", com.rudra.lifeledge.core.finance.engine.TransactionErrorCode.UNKNOWN_ERROR)
        
        // Calculate EMI breakdown
        val emiAmount = paymentAmount ?: loan.monthlyEMI
        
        // Get the principal and interest breakdown
        val breakdown = EmiCalculator.calculatePaymentBreakdown(
            principal = loan.remainingAmount,
            annualInterestRate = loan.interestRate,
            emi = emiAmount
        )
        
        // Validate not paying more than remaining
        if (emiAmount > loan.remainingAmount + breakdown.interest) {
            return TransactionResult.Error(
                "Payment exceeds remaining loan balance",
                com.rudra.lifeledge.core.finance.engine.TransactionErrorCode.INVALID_AMOUNT
            )
        }
        
        // Get source account
        val sourceAccount = fromAccountId?.let { accountRepository.getAccount(it) }
            ?: accountRepository.getMainBalanceAccount()
        
        // Get loan account
        val loanAccount = accountRepository.getLoanAccount(loanId)
        
        try {
            // Create journal entry for the payment
            // Debit: Loan account (reduces liability)
            // Credit: Bank/Cash account
            val result = transactionEngine.createJournalEntry(
                date = date,
                description = "EMI Payment - ${loan.name}",
                amount = emiAmount,
                type = TransactionType.EXPENSE, // EMI payment is an expense
                fromAccountId = sourceAccount.id,
                toAccountId = null,
                categoryId = loanAccount.id, // Link to loan account
                notes = notes ?: "EMI payment for loan #${loanId}"
            )
            
            if (result is TransactionResult.Error) {
                return result
            }
            
            // Update loan remaining amount
            val newRemainingAmount = loan.remainingAmount - breakdown.principal
            val updatedLoan = loan.copy(
                remainingAmount = maxOf(0.0, newRemainingAmount)
            )
            loanDao.updateLoan(updatedLoan)
            
            // Record the EMI payment
            val emiPayment = EMIPayment(
                loanId = loanId,
                date = date,
                amount = emiAmount,
                principalPaid = breakdown.principal,
                interestPaid = breakdown.interest,
                remainingBalance = maxOf(0.0, newRemainingAmount),
                isPaid = true
            )
            emiPaymentDao.insertEMIPayment(emiPayment)
            
            return result
        } catch (e: Exception) {
            return TransactionResult.Error(
                "Failed to process EMI payment: ${e.message}",
                com.rudra.lifeledge.core.finance.engine.TransactionErrorCode.UNKNOWN_ERROR
            )
        }
    }
    
    /**
     * Calculates the current EMI breakdown for a loan.
     */
    suspend fun calculateEmiBreakdown(loanId: Long): EmiCalculator.EMIBreakdown? {
        val loan = loanDao.getLoan(loanId) ?: return null
        
        return EmiCalculator.calculatePaymentBreakdown(
            principal = loan.remainingAmount,
            annualInterestRate = loan.interestRate,
            emi = loan.monthlyEMI
        )
    }
    
    /**
     * Gets all EMI payments for a loan.
     */
    fun getEmiPayments(loanId: Long) = emiPaymentDao.getEMIPaymentsForLoan(loanId)
    
    /**
     * Checks if a loan is fully paid off.
     */
    suspend fun isLoanPaidOff(loanId: Long): Boolean {
        val loan = loanDao.getLoan(loanId) ?: return false
        return loan.remainingAmount <= 0
    }
}
