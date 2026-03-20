package com.rudra.lifeledge.core.finance.usecase

import com.rudra.lifeledge.core.finance.engine.TransactionEngine
import com.rudra.lifeledge.core.finance.engine.TransactionResult
import com.rudra.lifeledge.data.local.dao.RecurringTransactionDao
import com.rudra.lifeledge.data.local.entity.RecurringTransaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Frequency of recurring transactions
 */
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Use case for managing recurring transactions (income/expense).
 * Supports both immediate execution and database scheduling.
 */
class RecurringTransactionUseCase(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val transferUseCase: TransferUseCase
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    /**
     * Creates a recurring transaction that can be stored in database for future execution.
     * 
     * @param type Transaction type (INCOME or EXPENSE)
     * @param amount The amount
     * @param categoryId Category for income/expense
     * @param accountId Source/destination account
     * @param frequency How often it repeats
     * @param startDate When to start
     * @param endDate When to end (null for indefinite)
     * @param notes Optional notes
     * @param payee Optional payee
     * @param executeNow If true, execute immediately in addition to scheduling
     */
    suspend fun createRecurring(
        type: TransactionType,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        frequency: RecurringFrequency,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        notes: String? = null,
        payee: String? = null,
        executeNow: Boolean = false
    ): Result<Long> {
        return try {
            // Map frequency to entity
            val entityFrequency = mapFrequencyToEntity(frequency)
            
            // Calculate next date
            val nextDate = calculateNextDate(startDate, frequency)
            
            // Create the recurring transaction record
            val recurring = RecurringTransaction(
                name = notes ?: "Recurring ${if (type == TransactionType.EXPENSE) "Expense" else "Income"}",
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                frequency = entityFrequency,
                interval = 1, // Default interval
                executeDay = startDate.dayOfMonth,
                startDate = startDate.format(dateFormatter),
                endDate = endDate?.format(dateFormatter),
                nextDate = nextDate.format(dateFormatter),
                lastExecutedDate = null,
                payee = payee,
                notes = notes,
                isActive = true
            )
            
            val recurringId = recurringTransactionDao.insertRecurringTransaction(recurring)
            
            // Execute now if requested
            if (executeNow) {
                executeNow(
                    type = type,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId,
                    notes = "Recurring: ${notes ?: (if (type == TransactionType.EXPENSE) "Expense" else "Income")}"
                )
            }
            
            Result.success(recurringId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Executes a recurring transaction immediately without saving to database.
     * 
     * @param type Transaction type
     * @param amount Amount
     * @param categoryId Category ID
     * @param accountId Account ID
     * @param notes Notes
     * @return TransactionResult
     */
    suspend fun executeNow(
        type: TransactionType,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        notes: String? = null
    ): TransactionResult {
        return when (type) {
            TransactionType.EXPENSE -> {
                addExpenseUseCase(
                    amount = amount,
                    categoryId = categoryId,
                    fromAccountId = accountId,
                    notes = notes
                )
            }
            
            TransactionType.INCOME -> {
                addIncomeUseCase(
                    amount = amount,
                    sourceId = categoryId,
                    sourceName = "Recurring",
                    toAccountId = accountId,
                    notes = notes
                )
            }
            
            else -> TransactionResult.Error(
                "Invalid type for execution",
                com.rudra.lifeledge.core.finance.engine.TransactionErrorCode.INVALID_AMOUNT
            )
        }
    }
    
    /**
     * Gets all active recurring transactions.
     */
    fun getActiveRecurring(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveRecurringTransactions()
    }
    
    /**
     * Gets all recurring transactions.
     */
    fun getAllRecurring(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getAllRecurringTransactions()
    }
    
    /**
     * Gets recurring transactions due for execution today.
     */
    suspend fun getDueRecurring(): List<RecurringTransaction> {
        val today = LocalDate.now().format(dateFormatter)
        return recurringTransactionDao.getRecurringTransactionsForDate(today)
    }
    
    /**
     * Processes all due recurring transactions.
     * Should be called daily by a background worker.
     */
    suspend fun processDueRecurring(): ProcessResult {
        val today = LocalDate.now().format(dateFormatter)
        val dueTransactions = recurringTransactionDao.getRecurringTransactionsForDate(today)
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()
        
        for (recurring in dueTransactions) {
            try {
                val result = executeNow(
                    type = recurring.type,
                    amount = recurring.amount,
                    categoryId = recurring.categoryId,
                    accountId = recurring.accountId,
                    notes = "Recurring: ${recurring.name}"
                )
                
                if (result is TransactionResult.Success) {
                    // Update next execution date
                    val currentNextDate = LocalDate.parse(recurring.nextDate)
                    val nextDate = calculateNextDateFromEntity(
                        currentDate = currentNextDate,
                        frequency = recurring.frequency
                    )
                    
                    // Check if we've passed the end date
                    val endDate = recurring.endDate?.let { LocalDate.parse(it) }
                    if (endDate != null && nextDate.isAfter(endDate)) {
                        // Deactivate the recurring transaction
                        recurringTransactionDao.updateRecurringTransaction(
                            recurring.copy(isActive = false)
                        )
                    } else {
                        // Update next execution date and last executed date
                        recurringTransactionDao.updateRecurringTransaction(
                            recurring.copy(
                                nextDate = nextDate.format(dateFormatter),
                                lastExecutedDate = today
                            )
                        )
                    }
                    successCount++
                } else if (result is TransactionResult.Error) {
                    failureCount++
                    errors.add("${recurring.name}: ${result.message}")
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("${recurring.name}: ${e.message}")
            }
        }
        
        return ProcessResult(
            processed = successCount + failureCount,
            success = successCount,
            failed = failureCount,
            errors = errors
        )
    }
    
    /**
     * Pauses a recurring transaction.
     */
    suspend fun pauseRecurring(recurringId: Long) {
        val recurring = recurringTransactionDao.getRecurringTransaction(recurringId)
        recurring?.let {
            recurringTransactionDao.updateRecurringTransaction(it.copy(isActive = false))
        }
    }
    
    /**
     * Resumes a paused recurring transaction.
     */
    suspend fun resumeRecurring(recurringId: Long) {
        val recurring = recurringTransactionDao.getRecurringTransaction(recurringId)
        recurring?.let {
            recurringTransactionDao.updateRecurringTransaction(it.copy(isActive = true))
        }
    }
    
    /**
     * Deletes a recurring transaction.
     */
    suspend fun deleteRecurring(recurringId: Long) {
        val recurring = recurringTransactionDao.getRecurringTransaction(recurringId)
        recurring?.let {
            recurringTransactionDao.deleteRecurringTransaction(it)
        }
    }
    
    /**
     * Calculates the next execution date based on frequency.
     */
    private fun calculateNextDate(
        currentDate: LocalDate,
        frequency: RecurringFrequency
    ): LocalDate {
        return when (frequency) {
            RecurringFrequency.DAILY -> currentDate.plusDays(1)
            RecurringFrequency.WEEKLY -> currentDate.plusWeeks(1)
            RecurringFrequency.BIWEEKLY -> currentDate.plusWeeks(2)
            RecurringFrequency.MONTHLY -> currentDate.plusMonths(1)
            RecurringFrequency.YEARLY -> currentDate.plusYears(1)
        }
    }
    
    /**
     * Calculates the next execution date from the entity's frequency.
     */
    private fun calculateNextDateFromEntity(
        currentDate: LocalDate,
        frequency: com.rudra.lifeledge.data.local.entity.Frequency
    ): LocalDate {
        return when (frequency) {
            com.rudra.lifeledge.data.local.entity.Frequency.DAILY -> currentDate.plusDays(1)
            com.rudra.lifeledge.data.local.entity.Frequency.WEEKLY -> currentDate.plusWeeks(1)
            com.rudra.lifeledge.data.local.entity.Frequency.MONTHLY -> currentDate.plusMonths(1)
            com.rudra.lifeledge.data.local.entity.Frequency.YEARLY -> currentDate.plusYears(1)
        }
    }
    
    /**
     * Maps our RecurringFrequency to the entity's Frequency enum.
     */
    private fun mapFrequencyToEntity(frequency: RecurringFrequency): com.rudra.lifeledge.data.local.entity.Frequency {
        return when (frequency) {
            RecurringFrequency.DAILY -> com.rudra.lifeledge.data.local.entity.Frequency.DAILY
            RecurringFrequency.WEEKLY -> com.rudra.lifeledge.data.local.entity.Frequency.WEEKLY
            RecurringFrequency.BIWEEKLY -> com.rudra.lifeledge.data.local.entity.Frequency.WEEKLY // BIWEEKLY mapped to WEEKLY with interval
            RecurringFrequency.MONTHLY -> com.rudra.lifeledge.data.local.entity.Frequency.MONTHLY
            RecurringFrequency.YEARLY -> com.rudra.lifeledge.data.local.entity.Frequency.YEARLY
        }
    }
    
    /**
     * Maps entity's Frequency to our RecurringFrequency.
     */
    private fun mapEntityToFrequency(frequency: com.rudra.lifeledge.data.local.entity.Frequency): RecurringFrequency {
        return when (frequency) {
            com.rudra.lifeledge.data.local.entity.Frequency.DAILY -> RecurringFrequency.DAILY
            com.rudra.lifeledge.data.local.entity.Frequency.WEEKLY -> RecurringFrequency.WEEKLY
            // BIWEEKLY maps to WEEKLY since it's not in the enum
            com.rudra.lifeledge.data.local.entity.Frequency.MONTHLY -> RecurringFrequency.MONTHLY
            com.rudra.lifeledge.data.local.entity.Frequency.YEARLY -> RecurringFrequency.YEARLY
        }
    }
    
    /**
     * Result of processing due recurring transactions.
     */
    data class ProcessResult(
        val processed: Int,
        val success: Int,
        val failed: Int,
        val errors: List<String>
    )
}
