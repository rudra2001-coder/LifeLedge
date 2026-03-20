package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySummaryDao {
    @Query("SELECT * FROM monthly_summaries WHERE yearMonth = :yearMonth")
    suspend fun getMonthlySummary(yearMonth: String): MonthlySummaryEntity?

    @Query("SELECT * FROM monthly_summaries ORDER BY yearMonth DESC LIMIT :limit")
    fun getRecentMonthlySummaries(limit: Int): Flow<List<MonthlySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlySummary(summary: MonthlySummaryEntity)

    @Delete
    suspend fun deleteMonthlySummary(summary: MonthlySummaryEntity)

    @Query("DELETE FROM monthly_summaries")
    suspend fun clearAll()
}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    suspend fun getDailySummary(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailySummariesBetween(startDate: String, endDate: String): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    fun getRecentDailySummaries(limit: Int): Flow<List<DailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailySummaryEntity)

    @Delete
    suspend fun deleteDailySummary(summary: DailySummaryEntity)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAll()
}

@Dao
interface CategorySummaryDao {
    @Query("SELECT * FROM category_summaries WHERE yearMonth = :yearMonth ORDER BY amount DESC")
    fun getCategorySummariesForMonth(yearMonth: String): Flow<List<CategorySummaryEntity>>

    @Query("SELECT * FROM category_summaries WHERE yearMonth = :yearMonth AND categoryId = :categoryId")
    suspend fun getCategorySummary(yearMonth: String, categoryId: Long): CategorySummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorySummary(summary: CategorySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorySummaries(summaries: List<CategorySummaryEntity>)

    @Query("DELETE FROM category_summaries WHERE yearMonth = :yearMonth")
    suspend fun deleteCategorySummariesForMonth(yearMonth: String)

    @Query("DELETE FROM category_summaries")
    suspend fun clearAll()
}

@Dao
interface AccountSummaryDao {
    @Query("SELECT * FROM account_summaries WHERE accountId = :accountId")
    suspend fun getAccountSummary(accountId: Long): AccountSummaryEntity?

    @Query("SELECT * FROM account_summaries")
    fun getAllAccountSummaries(): Flow<List<AccountSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountSummary(summary: AccountSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountSummaries(summaries: List<AccountSummaryEntity>)

    @Query("DELETE FROM account_summaries WHERE accountId = :accountId")
    suspend fun deleteAccountSummary(accountId: Long)

    @Query("DELETE FROM account_summaries")
    suspend fun clearAll()
}

@Dao
interface BehaviorPatternDao {
    @Query("SELECT * FROM behavior_patterns ORDER BY dayOfWeek, hourOfDay")
    fun getAllBehaviorPatterns(): Flow<List<BehaviorPatternEntity>>

    @Query("SELECT * FROM behavior_patterns WHERE dayOfWeek = :dayOfWeek")
    fun getPatternsForDay(dayOfWeek: Int): Flow<List<BehaviorPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehaviorPattern(pattern: BehaviorPatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehaviorPatterns(patterns: List<BehaviorPatternEntity>)

    @Query("DELETE FROM behavior_patterns")
    suspend fun clearAll()
}

@Dao
interface SpendingStreakDao {
    @Query("SELECT * FROM spending_streaks WHERE streakType = :streakType")
    suspend fun getStreak(streakType: String): SpendingStreakEntity?

    @Query("SELECT * FROM spending_streaks")
    fun getAllStreaks(): Flow<List<SpendingStreakEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: SpendingStreakEntity)

    @Query("DELETE FROM spending_streaks")
    suspend fun clearAll()
}
