package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_summaries")
data class MonthlySummaryEntity(
    @PrimaryKey
    val yearMonth: String,
    val income: Double,
    val expense: Double,
    val savings: Double,
    val savingsRate: Double,
    val transactionCount: Int,
    val lastUpdated: String
)

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey
    val date: String,
    val income: Double,
    val expense: Double,
    val savings: Double,
    val transactionCount: Int,
    val lastUpdated: String
)

@Entity(tableName = "category_summaries")
data class CategorySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val yearMonth: String,
    val categoryId: Long,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int,
    val lastUpdated: String
)

@Entity(tableName = "account_summaries")
data class AccountSummaryEntity(
    @PrimaryKey
    val accountId: Long,
    val totalIncome: Double,
    val totalExpense: Double,
    val totalSaved: Double,
    val currentBalance: Double,
    val transactionCount: Int,
    val lastUpdated: String
)

@Entity(tableName = "behavior_patterns")
data class BehaviorPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int,
    val hourOfDay: Int,
    val averageSpending: Double,
    val transactionCount: Int,
    val mostFrequentCategory: Long?,
    val lastUpdated: String
)

@Entity(tableName = "spending_streaks")
data class SpendingStreakEntity(
    @PrimaryKey
    val streakType: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastStreakDate: String,
    val totalUnderBudgetDays: Int
)
