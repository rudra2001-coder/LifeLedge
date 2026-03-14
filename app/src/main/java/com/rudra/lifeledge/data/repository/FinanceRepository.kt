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

    suspend fun saveRecurringTransaction(recurringTransaction: RecurringTransaction): Long =
        recurringTransactionDao.insertRecurringTransaction(recurringTransaction)

    fun getAllLoans(): Flow<List<Loan>> = loanDao.getAllLoans()

    fun getTotalRemainingLoanAmount(): Flow<Double?> = loanDao.getTotalRemainingAmount()

    suspend fun saveLoan(loan: Loan): Long = loanDao.insertLoan(loan)

    fun getAllCreditCards(): Flow<List<CreditCard>> = creditCardDao.getAllCreditCards()

    suspend fun saveCreditCard(creditCard: CreditCard): Long = creditCardDao.insertCreditCard(creditCard)
}
