package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.JournalLine
import kotlinx.coroutines.flow.Flow

/**
 * Data class for account balance information
 */
data class AccountBalance(
    val accountId: Long,
    val balance: Double
)

/**
 * Data class for journal entry with lines
 */
data class JournalEntryWithLines(
    val id: Long,
    val date: String,
    val description: String,
    val createdAt: String
)

/**
 * DAO for JournalLine operations in double-entry accounting.
 */
@Dao
interface JournalLineDao {
    
    /**
     * Insert a single journal line
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalLine(journalLine: JournalLine): Long
    
    /**
     * Insert multiple journal lines in a batch
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalLines(journalLines: List<JournalLine>)
    
    /**
     * Get all journal lines for a specific journal entry
     */
    @Query("SELECT * FROM journal_lines WHERE journalEntryId = :journalEntryId")
    fun getLinesForEntry(journalEntryId: Long): Flow<List<JournalLine>>
    
    /**
     * Get all journal lines for a specific journal entry (suspend)
     */
    @Query("SELECT * FROM journal_lines WHERE journalEntryId = :journalEntryId")
    suspend fun getLinesForEntrySync(journalEntryId: Long): List<JournalLine>
    
    /**
     * Get all journal lines for a specific account
     */
    @Query("SELECT * FROM journal_lines WHERE accountId = :accountId ORDER BY journalEntryId DESC")
    fun getLinesForAccount(accountId: Long): Flow<List<JournalLine>>
    
    /**
     * Get journal lines between dates for a specific account
     */
    @Query("""
        SELECT jl.* FROM journal_lines jl
        INNER JOIN journal_entries je ON jl.journalEntryId = je.id
        WHERE jl.accountId = :accountId 
        AND je.date BETWEEN :startDate AND :endDate
        ORDER BY je.date DESC
    """)
    fun getLinesForAccountInRange(
        accountId: Long, 
        startDate: String, 
        endDate: String
    ): Flow<List<JournalLine>>
    
    /**
     * Calculate the balance for a specific account.
     * Balance = Sum of Debits - Sum of Credits
     * 
     * For Asset and Expense accounts: normal balance is debit (positive result)
     * For Liability, Equity, and Income accounts: normal balance is credit (negative result means credit balance)
     */
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(debit) FROM journal_lines WHERE accountId = :accountId) -
            (SELECT SUM(credit) FROM journal_lines WHERE accountId = :accountId),
            0.0
        )
    """)
    suspend fun getAccountBalance(accountId: Long): Double
    
    /**
     * Get account balance as Flow for reactive updates
     */
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(debit) FROM journal_lines WHERE accountId = :accountId) -
            (SELECT SUM(credit) FROM journal_lines WHERE accountId = :accountId),
            0.0
        )
    """)
    fun getAccountBalanceFlow(accountId: Long): Flow<Double>
    
    /**
     * Get balances for multiple accounts at once
     */
    @Query("""
        SELECT accountId, 
            COALESCE(SUM(debit) - SUM(credit), 0.0) as balance
        FROM journal_lines 
        WHERE accountId IN (:accountIds)
        GROUP BY accountId
    """)
    fun getAccountBalances(accountIds: List<Long>): Flow<List<AccountBalance>>
    
    /**
     * Get total debits for an account in a date range
     */
    @Query("""
        SELECT COALESCE(SUM(jl.debit), 0.0)
        FROM journal_lines jl
        INNER JOIN journal_entries je ON jl.journalEntryId = je.id
        WHERE jl.accountId = :accountId
        AND je.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getDebitsInRange(
        accountId: Long, 
        startDate: String, 
        endDate: String
    ): Double
    
    /**
     * Get total credits for an account in a date range
     */
    @Query("""
        SELECT COALESCE(SUM(jl.credit), 0.0)
        FROM journal_lines jl
        INNER JOIN journal_entries je ON jl.journalEntryId = je.id
        WHERE jl.accountId = :accountId
        AND je.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getCreditsInRange(
        accountId: Long, 
        startDate: String, 
        endDate: String
    ): Double
    
    /**
     * Delete a journal line
     */
    @Delete
    suspend fun deleteJournalLine(journalLine: JournalLine)
    
    /**
     * Delete all journal lines for a specific journal entry
     */
    @Query("DELETE FROM journal_lines WHERE journalEntryId = :journalEntryId")
    suspend fun deleteLinesForEntry(journalEntryId: Long)
    
    /**
     * Validate that debits equal credits for a journal entry
     */
    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(debit), 0.0) FROM journal_lines WHERE journalEntryId = :journalEntryId) =
            (SELECT COALESCE(SUM(credit), 0.0) FROM journal_lines WHERE journalEntryId = :journalEntryId)
    """)
    suspend fun isBalanced(journalEntryId: Long): Boolean
    
    /**
     * Get total debits for a journal entry
     */
    @Query("SELECT COALESCE(SUM(debit), 0.0) FROM journal_lines WHERE journalEntryId = :journalEntryId")
    suspend fun getTotalDebits(journalEntryId: Long): Double
    
    /**
     * Get total credits for a journal entry
     */
    @Query("SELECT COALESCE(SUM(credit), 0.0) FROM journal_lines WHERE journalEntryId = :journalEntryId")
    suspend fun getTotalCredits(journalEntryId: Long): Double
}
