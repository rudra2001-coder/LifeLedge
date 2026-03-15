package com.rudra.lifeledge.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val db = LifeLedgerDatabase.getDatabase(applicationContext)
            val repository = com.rudra.lifeledge.data.repository.FinanceRepository(
                db.accountDao(),
                db.transactionDao(),
                db.categoryDao(),
                db.recurringTransactionDao(),
                db.loanDao(),
                db.emiPaymentDao(),
                db.creditCardDao()
            )

            val dueTransactions = repository.getDueRecurringTransactions(today)

            for (recurring in dueTransactions) {
                if (recurring.endDate != null && LocalDate.parse(recurring.endDate) < LocalDate.parse(today)) {
                    continue
                }

                repository.processRecurringTransaction(recurring, today)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "recurring_transaction_worker"
    }
}
