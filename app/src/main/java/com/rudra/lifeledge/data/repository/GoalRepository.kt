package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.GoalDao
import com.rudra.lifeledge.data.local.dao.HealthMetricDao
import com.rudra.lifeledge.data.local.entity.Goal
import com.rudra.lifeledge.data.local.entity.HealthMetric
import com.rudra.lifeledge.data.local.entity.GoalType
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val goalDao: GoalDao,
    private val healthMetricDao: HealthMetricDao
) {
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()

    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getActiveGoals()

    fun getGoalsByType(type: GoalType): Flow<List<Goal>> = goalDao.getGoalsByType(type)

    fun getCompletedGoals(): Flow<List<Goal>> = goalDao.getCompletedGoals()

    suspend fun getGoal(id: Long): Goal? = goalDao.getGoal(id)

    suspend fun saveGoal(goal: Goal): Long = goalDao.insertGoal(goal)

    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    fun getAllHealthMetrics(): Flow<List<HealthMetric>> = healthMetricDao.getAllHealthMetrics()

    fun getHealthMetricsBetween(startDate: String, endDate: String): Flow<List<HealthMetric>> =
        healthMetricDao.getHealthMetricsBetween(startDate, endDate)

    suspend fun getHealthMetricForDate(date: String): HealthMetric? =
        healthMetricDao.getHealthMetricForDate(date)

    suspend fun saveHealthMetric(healthMetric: HealthMetric): Long =
        healthMetricDao.insertHealthMetric(healthMetric)
}
