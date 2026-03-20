package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * JournalLine represents a single line in a double-entry accounting journal entry.
 * Each transaction creates journal lines that maintain the accounting equation:
 * Assets = Liabilities + Equity
 * 
 * In double-entry accounting:
 * - Every transaction has at least two journal lines (debits and credits)
 * - The total debits must equal the total credits for each transaction
 * - Debits increase asset and expense accounts
 * - Credits increase liability, equity, and income accounts
 */
@Entity(
    tableName = "journal_lines",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntry::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["journalEntryId"]),
        Index(value = ["accountId"])
    ]
)
data class JournalLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** The journal entry this line belongs to */
    val journalEntryId: Long,
    
    /** The account this line affects */
    val accountId: Long,
    
    /** Debit amount (positive for debits, 0 for credits) */
    val debit: Double = 0.0,
    
    /** Credit amount (positive for credits, 0 for debits) */
    val credit: Double = 0.0,
    
    /** Optional memo/description for this specific line */
    val memo: String? = null
) {
    /**
     * Validates that this journal line is valid.
     * A valid line must have either a debit OR a credit, but not both.
     */
    fun isValid(): Boolean {
        return (debit > 0 && credit == 0.0) || (credit > 0 && debit == 0.0)
    }
    
    /**
     * Returns the amount (either debit or credit, whichever is positive)
     */
    fun getAmount(): Double = if (debit > 0) debit else credit
}

/**
 * Extended JournalEntry with lines for complete double-entry transactions.
 * This is used for creating and querying transactions with full journal details.
 */
data class JournalEntryWithLines(
    val id: Long = 0,
    val date: String,
    val description: String,
    val createdAt: String,
    val lines: List<JournalLine>
)
