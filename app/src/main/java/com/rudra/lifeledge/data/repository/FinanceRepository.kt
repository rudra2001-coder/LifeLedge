package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.dao.CategoryDao
import com.rudra.lifeledge.data.local.dao.RecurringTransactionDao
import com.rudra.lifeledge.data.local.dao.LoanDao
import com.rudra.lifeledge.data.local.dao.EMIPaymentDao
import com.rudra.lifeledge.data.local.dao.CreditCardDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.Category
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.Loan
import com.rudra.lifeledge.data.local.entity.EMIPayment
import com.rudra.lifeledge.data.local.entity.CreditCard
import com.rudra.lifeledge.data.local.entity.TransactionType
import com.rudra.lifeledge.data.local.entity.AccountType
import com.rudra.lifeledge.data.local.entity.Frequency
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val loanDao: LoanDao,
    private val emiPaymentDao: EMIPaymentDao,
    private val creditCardDao: CreditCardDao
) {
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    fun getActiveAccounts(): Flow<List<Account>> = accountDao.getActiveAccounts()

    fun getTotalBalance(): Flow<Double?> = accountDao.getTotalBalance()

    suspend fun getAccount(id: Long): Account? = accountDao.getAccount(id)

    suspend fun saveAccount(account: Account): Long = accountDao.insertAccount(account)

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getActiveCategories(): Flow<List<Category>> = categoryDao.getActiveCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategory(id: Long): Category? = categoryDao.getCategory(id)

    suspend fun saveCategory(category: Category): Long = categoryDao.insertCategory(category)

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getRecentTransactions(100)

    fun getTransactionsForDate(date: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsForDate(date)

    fun getTransactionsBetween(startDate: String, endDate: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetween(startDate, endDate)

    fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getTransaction(id)

    suspend fun saveTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    suspend fun getTotalIncome(startDate: String, endDate: String): Double =
        transactionDao.getTotalByType(TransactionType.INCOME, startDate, endDate) ?: 0.0

    suspend fun getTotalExpense(startDate: String, endDate: String): Double =
        transactionDao.getTotalByType(TransactionType.EXPENSE, startDate, endDate) ?: 0.0

    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getActiveRecurringTransactions()

    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getAllRecurringTransactions()

    suspend fun saveRecurringTransaction(recurringTransaction: RecurringTransaction): Long =
        recurringTransactionDao.insertRecurringTransaction(recurringTransaction)

    fun getAllLoans(): Flow<List<Loan>> = loanDao.getAllLoans()

    fun getTotalRemainingLoanAmount(): Flow<Double?> = loanDao.getTotalRemainingAmount()

    suspend fun saveLoan(loan: Loan): Long = loanDao.insertLoan(loan)

    fun getAllCreditCards(): Flow<List<CreditCard>> = creditCardDao.getAllCreditCards()

    suspend fun saveCreditCard(creditCard: CreditCard): Long = creditCardDao.insertCreditCard(creditCard)

    // Balance Calculations
    fun getTotalIncome(): Flow<Double> = transactionDao.getTotalIncome()
    fun getTotalExpense(): Flow<Double> = transactionDao.getTotalExpense()
    fun getTotalSaved(): Flow<Double> = transactionDao.getTotalSaved()
    fun getTotalTransferredFromSavings(): Flow<Double> = transactionDao.getTotalTransferredFromSavings()
    fun getNetBalance(): Flow<Double> = transactionDao.getNetBalance()
    fun getSavingsBalance(): Flow<Double> = transactionDao.getSavingsBalance()

    // Monthly totals
    fun getMonthlyIncome(startDate: String, endDate: String): Flow<Double> = 
        transactionDao.getMonthlyIncome(startDate, endDate)
    fun getMonthlyExpense(startDate: String, endDate: String): Flow<Double> = 
        transactionDao.getMonthlyExpense(startDate, endDate)
    fun getMonthlySaved(startDate: String, endDate: String): Flow<Double> = 
        transactionDao.getMonthlySaved(startDate, endDate)

    // Money Flow Actions
    suspend fun addIncome(amount: Double, category: String?, note: String? = null) {
        val transaction = Transaction(
            date = java.time.LocalDate.now().toString(),
            amount = amount,
            type = TransactionType.INCOME,
            categoryId = 0,
            accountId = 0,
            toAccountId = null,
            payee = null,
            notes = note,
            isRecurring = false,
            recurringId = null,
            cardId = null,
            isCleared = true,
            attachment = null,
            location = null,
            tags = category ?: ""
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun addExpense(amount: Double, category: String?, note: String? = null) {
        val transaction = Transaction(
            date = java.time.LocalDate.now().toString(),
            amount = amount,
            type = TransactionType.EXPENSE,
            categoryId = 0,
            accountId = 0,
            toAccountId = null,
            payee = null,
            notes = note,
            isRecurring = false,
            recurringId = null,
            cardId = null,
            isCleared = true,
            attachment = null,
            location = null,
            tags = category ?: ""
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun addSaving(amount: Double, note: String? = null) {
        val transaction = Transaction(
            date = java.time.LocalDate.now().toString(),
            amount = amount,
            type = TransactionType.SAVE,
            categoryId = 0,
            accountId = 0,
            toAccountId = null,
            payee = null,
            notes = note,
            isRecurring = false,
            recurringId = null,
            cardId = null,
            isCleared = true,
            attachment = null,
            location = null,
            tags = ""
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun transferFromSavings(amount: Double, note: String? = null) {
        val transaction = Transaction(
            date = java.time.LocalDate.now().toString(),
            amount = amount,
            type = TransactionType.TRANSFER_FROM_SAVING,
            categoryId = 0,
            accountId = 0,
            toAccountId = null,
            payee = null,
            notes = note,
            isRecurring = false,
            recurringId = null,
            cardId = null,
            isCleared = true,
            attachment = null,
            location = null,
            tags = ""
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getDueRecurringTransactions(date: String): List<RecurringTransaction> =
        recurringTransactionDao.getDueRecurringTransactions(date)

    suspend fun getRecurringTransactionsForDate(date: String): List<RecurringTransaction> =
        recurringTransactionDao.getRecurringTransactionsForDate(date)

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) =
        recurringTransactionDao.updateRecurringTransaction(recurringTransaction)

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) =
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction)

    suspend fun setRecurringActive(id: Long, isActive: Boolean) =
        recurringTransactionDao.setActive(id, isActive)

    suspend fun processRecurringTransaction(
        recurringTransaction: RecurringTransaction,
        currentDate: String
    ): Transaction {
        val transaction = Transaction(
            date = currentDate,
            amount = recurringTransaction.amount,
            type = recurringTransaction.type,
            categoryId = recurringTransaction.categoryId,
            accountId = recurringTransaction.accountId,
            toAccountId = null,
            payee = recurringTransaction.payee,
            notes = recurringTransaction.notes,
            isRecurring = true,
            recurringId = recurringTransaction.id,
            cardId = null,
            isCleared = true,
            attachment = null,
            location = null,
            tags = ""
        )
        val transactionId = transactionDao.insertTransaction(transaction)

        val nextDate = calculateNextDate(
            currentDate,
            recurringTransaction.frequency,
            recurringTransaction.interval,
            recurringTransaction.executeDay
        )
        val updatedRecurring = recurringTransaction.copy(
            nextDate = nextDate,
            lastExecutedDate = currentDate
        )
        recurringTransactionDao.updateRecurringTransaction(updatedRecurring)

        return transaction.copy(id = transactionId)
    }

    private fun calculateNextDate(
        currentDate: String,
        frequency: Frequency,
        interval: Int,
        executeDay: Int?
    ): String {
        val today = java.time.LocalDate.parse(currentDate)
        return when (frequency) {
            Frequency.DAILY -> today.plusDays(interval.toLong())
            Frequency.WEEKLY -> today.plusWeeks(interval.toLong())
            Frequency.MONTHLY -> {
                if (executeDay != null) {
                    val nextMonth = today.plusMonths(interval.toLong())
                    val dayOfMonth = minOf(executeDay, nextMonth.lengthOfMonth())
                    nextMonth.withDayOfMonth(dayOfMonth)
                } else {
                    today.plusMonths(interval.toLong())
                }
            }
            Frequency.YEARLY -> today.plusYears(interval.toLong())
        }.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    }

    suspend fun getRecurringTransaction(id: Long): RecurringTransaction? =
        recurringTransactionDao.getRecurringTransaction(id)

    // Daily totals
    fun getDailyIncome(date: String): Flow<Double> = transactionDao.getDailyIncome(date)
    fun getDailyExpense(date: String): Flow<Double> = transactionDao.getDailyExpense(date)

    // Expense by category
    fun getExpensesByCategory(startDate: String, endDate: String): Flow<List<CategoryTotal>> =
        transactionDao.getExpensesByCategory(startDate, endDate)
}
