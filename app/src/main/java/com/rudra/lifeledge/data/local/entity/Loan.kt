package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LoanType { PERSONAL, HOME, CAR, EDUCATION, OTHER }

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: LoanType,
    val totalAmount: Double,
    val remainingAmount: Double,
    val interestRate: Double,
    val startDate: String,
    val endDate: String,
    val lender: String,
    val monthlyEMI: Double,
    val notes: String?
)
