package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.HealthMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    @Query("SELECT * FROM health_metrics WHERE id = :id")
    suspend fun getHealthMetric(id: Long): HealthMetric?

    @Query("SELECT * FROM health_metrics WHERE date = :date")
    suspend fun getHealthMetricForDate(date: String): HealthMetric?

    @Query("SELECT * FROM health_metrics WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getHealthMetricsBetween(startDate: String, endDate: String): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics ORDER BY date DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(healthMetric: HealthMetric): Long

    @Update
    suspend fun updateHealthMetric(healthMetric: HealthMetric)

    @Delete
    suspend fun deleteHealthMetric(healthMetric: HealthMetric)
}
