package com.rudra.lifeledge.core.finance.safety

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.Transaction
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrustAndSafetyManager(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val recentlyDeletedTransactions = mutableListOf<DeletedTransactionRecord>()
    private var lastOperation: OperationRecord? = null

    companion object {
        const val UNDO_TIMEOUT_MS = 10000L
        const val MAX_UNDO_HISTORY = 10
    }

    data class DeletedTransactionRecord(
        val transaction: Transaction,
        val deletedAt: Long,
        val accountSnapshot: Map<Long, Double>
    )

    data class OperationRecord(
        val type: OperationType,
        val timestamp: Long,
        val data: Any?
    )

    enum class OperationType {
        ADD, DELETE, UPDATE, TRANSFER
    }

    @androidx.room.Transaction
    suspend fun safeDeleteTransaction(transaction: Transaction): DeleteResult {
        val accountSnapshots = mutableMapOf<Long, Double>()
        
        val accounts = accountDao.getAllAccounts().first()
        for (account in accounts) {
            accountSnapshots[account.id] = account.balance
        }

        transactionDao.deleteTransaction(transaction)

        recentlyDeletedTransactions.add(
            DeletedTransactionRecord(
                transaction = transaction,
                deletedAt = System.currentTimeMillis(),
                accountSnapshot = accountSnapshots
            )
        )

        if (recentlyDeletedTransactions.size > MAX_UNDO_HISTORY) {
            recentlyDeletedTransactions.removeAt(0)
        }

        lastOperation = OperationRecord(
            type = OperationType.DELETE,
            timestamp = System.currentTimeMillis(),
            data = transaction
        )

        return DeleteResult(
            success = true,
            canUndo = true,
            undoDeadline = System.currentTimeMillis() + UNDO_TIMEOUT_MS
        )
    }

    suspend fun undoLastDelete(): UndoResult {
        val lastDeleted = recentlyDeletedTransactions.lastOrNull() ?: return UndoResult(
            success = false,
            message = "No recent deletions to undo"
        )

        if (System.currentTimeMillis() - lastDeleted.deletedAt > UNDO_TIMEOUT_MS) {
            recentlyDeletedTransactions.removeLast()
            return UndoResult(
                success = false,
                message = "Undo window has expired (10 seconds)"
            )
        }

        transactionDao.insertTransaction(lastDeleted.transaction)

        recentlyDeletedTransactions.removeLast()

        lastOperation = OperationRecord(
            type = OperationType.ADD,
            timestamp = System.currentTimeMillis(),
            data = lastDeleted.transaction
        )

        return UndoResult(
            success = true,
            message = "Transaction restored successfully"
        )
    }

    fun canUndo(): Boolean {
        val lastDeleted = recentlyDeletedTransactions.lastOrNull() ?: return false
        return System.currentTimeMillis() - lastDeleted.deletedAt < UNDO_TIMEOUT_MS
    }

    fun getUndoTimeRemaining(): Long {
        val lastDeleted = recentlyDeletedTransactions.lastOrNull() ?: return 0
        val remaining = UNDO_TIMEOUT_MS - (System.currentTimeMillis() - lastDeleted.deletedAt)
        return if (remaining > 0) remaining else 0
    }

    suspend fun validateDataIntegrity(): IntegrityValidationResult {
        val issues = mutableListOf<IntegrityIssue>()
        
        val accounts = accountDao.getAllAccounts().first()
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()

        for (account in accounts) {
            val accountTxns = transactions.filter { it.accountId == account.id }
            
            val calculatedBalance = account.balance +
                    accountTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } -
                    accountTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
                    accountTxns.filter { it.type == TransactionType.SAVE }.sumOf { it.amount } +
                    accountTxns.filter { it.type == TransactionType.TRANSFER_FROM_SAVING }.sumOf { it.amount }

            if (kotlin.math.abs(calculatedBalance - account.balance) > 0.01) {
                issues.add(
                    IntegrityIssue(
                        type = IssueType.BALANCE_MISMATCH,
                        severity = IssueSeverity.HIGH,
                        description = "Account '${account.name}' has balance mismatch",
                        affectedEntityId = account.id,
                        expectedValue = calculatedBalance,
                        actualValue = account.balance
                    )
                )
            }
        }

        val duplicateTransactions = transactions
            .groupBy { "${it.date}_${it.amount}_${it.type}" }
            .filter { it.value.size > 1 }

        if (duplicateTransactions.isNotEmpty()) {
            issues.add(
                IntegrityIssue(
                    type = IssueType.DUPLICATE_TRANSACTIONS,
                    severity = IssueSeverity.MEDIUM,
                    description = "Found ${duplicateTransactions.values.flatten().size} potential duplicate transactions",
                    affectedEntityId = null,
                    expectedValue = 0.0,
                    actualValue = duplicateTransactions.values.flatten().size.toDouble()
                )
            )
        }

        val futureTransactions = transactions.filter {
            try {
                LocalDate.parse(it.date).isAfter(LocalDate.now())
            } catch (e: Exception) {
                false
            }
        }

        if (futureTransactions.isNotEmpty()) {
            issues.add(
                IntegrityIssue(
                    type = IssueType.FUTURE_DATES,
                    severity = IssueSeverity.LOW,
                    description = "Found ${futureTransactions.size} transactions with future dates",
                    affectedEntityId = null,
                    expectedValue = 0.0,
                    actualValue = futureTransactions.size.toDouble()
                )
            )
        }

        val negativeAmounts = transactions.filter { it.amount < 0 }
        if (negativeAmounts.isNotEmpty()) {
            issues.add(
                IntegrityIssue(
                    type = IssueType.NEGATIVE_AMOUNTS,
                    severity = IssueSeverity.HIGH,
                    description = "Found ${negativeAmounts.size} transactions with negative amounts",
                    affectedEntityId = null,
                    expectedValue = 0.0,
                    actualValue = negativeAmounts.size.toDouble()
                )
            )
        }

        return IntegrityValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            totalTransactionsChecked = transactions.size.toLong(),
            totalAccountsChecked = accounts.size,
            validatedAt = LocalDate.now().format(dateFormatter)
        )
    }

    suspend fun autoFixIntegrityIssues(): FixResult {
        val result = validateDataIntegrity()
        val fixes = mutableListOf<String>()

        for (issue in result.issues) {
            when (issue.type) {
                IssueType.BALANCE_MISMATCH -> {
                    val account = accountDao.getAccount(issue.affectedEntityId!!)
                    if (account != null) {
                        accountDao.updateAccount(account.copy(balance = issue.expectedValue))
                        fixes.add("Fixed balance for account ID ${issue.affectedEntityId}")
                    }
                }
                IssueType.DUPLICATE_TRANSACTIONS,
                IssueType.FUTURE_DATES,
                IssueType.NEGATIVE_AMOUNTS,
                IssueType.ORPHANED_TRANSACTIONS -> {
                    fixes.add("Manual review required for ${issue.type}")
                }
            }
        }

        return FixResult(
            success = fixes.isNotEmpty(),
            fixesApplied = fixes,
            remainingIssues = result.issues.size - fixes.size
        )
    }

    suspend fun createBackupSnapshot(): BackupSnapshot {
        val accounts = accountDao.getAllAccounts().first()
        val transactions = transactionDao.getRecentTransactions(Int.MAX_VALUE).first()

        return BackupSnapshot(
            accountsCount = accounts.size,
            transactionsCount = transactions.size,
            totalBalance = accounts.sumOf { it.balance },
            createdAt = LocalDate.now().format(dateFormatter),
            checksum = generateChecksum(accounts, transactions)
        )
    }

    private fun generateChecksum(accounts: List<Account>, transactions: List<Transaction>): String {
        var hash = 0
        accounts.forEach { hash += it.id.hashCode() + it.balance.hashCode() }
        transactions.forEach { hash += it.id.hashCode() + it.amount.hashCode() }
        return hash.toString(16)
    }

    fun getLastOperation(): OperationRecord? = lastOperation
}

data class DeleteResult(
    val success: Boolean,
    val canUndo: Boolean,
    val undoDeadline: Long
)

data class UndoResult(
    val success: Boolean,
    val message: String
)

data class IntegrityValidationResult(
    val isValid: Boolean,
    val issues: List<IntegrityIssue>,
    val totalTransactionsChecked: Long,
    val totalAccountsChecked: Int,
    val validatedAt: String
)

data class IntegrityIssue(
    val type: IssueType,
    val severity: IssueSeverity,
    val description: String,
    val affectedEntityId: Long?,
    val expectedValue: Double,
    val actualValue: Double
)

enum class IssueType {
    BALANCE_MISMATCH,
    DUPLICATE_TRANSACTIONS,
    FUTURE_DATES,
    NEGATIVE_AMOUNTS,
    ORPHANED_TRANSACTIONS
}

enum class IssueSeverity { LOW, MEDIUM, HIGH, CRITICAL }

data class FixResult(
    val success: Boolean,
    val fixesApplied: List<String>,
    val remainingIssues: Int
)

data class BackupSnapshot(
    val accountsCount: Int,
    val transactionsCount: Int,
    val totalBalance: Double,
    val createdAt: String,
    val checksum: String
)
