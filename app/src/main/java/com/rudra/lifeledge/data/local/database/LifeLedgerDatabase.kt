package com.rudra.lifeledge.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.lifeledge.data.local.converters.Converters
import com.rudra.lifeledge.data.local.dao.*
import com.rudra.lifeledge.data.local.entity.*

@Database(
    entities = [
        WorkLog::class, WorkDay::class, OvertimeLog::class, WorkSession::class,
        Account::class, Transaction::class, Category::class, RecurringTransaction::class,
        Loan::class, EMIPayment::class, CreditCard::class,
        Habit::class, HabitCompletion::class,
        JournalEntry::class, DailyLog::class,
        Goal::class, HealthMetric::class,
        Setting::class, BackupLog::class, SmartAdviceLog::class,
        SavingGoal::class, SavingTransaction::class,
        CardEntity::class,
        ActivityLog::class,
        MonthlySummaryEntity::class, DailySummaryEntity::class, CategorySummaryEntity::class,
        AccountSummaryEntity::class, BehaviorPatternEntity::class, SpendingStreakEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeLedgerDatabase : RoomDatabase() {
    abstract fun workLogDao(): WorkLogDao
    abstract fun workDayDao(): WorkDayDao
    abstract fun overtimeLogDao(): OvertimeLogDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun emiPaymentDao(): EMIPaymentDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun goalDao(): GoalDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun settingDao(): SettingDao
    abstract fun backupLogDao(): BackupLogDao
    abstract fun smartAdviceLogDao(): SmartAdviceLogDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun savingTransactionDao(): SavingTransactionDao
    abstract fun cardDao(): CardDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun categorySummaryDao(): CategorySummaryDao
    abstract fun accountSummaryDao(): AccountSummaryDao
    abstract fun behaviorPatternDao(): BehaviorPatternDao
    abstract fun spendingStreakDao(): SpendingStreakDao

    companion object {
        @Volatile
        private var INSTANCE: LifeLedgerDatabase? = null

        fun getDatabase(context: Context): LifeLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeLedgerDatabase::class.java,
                    "lifeledger_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
