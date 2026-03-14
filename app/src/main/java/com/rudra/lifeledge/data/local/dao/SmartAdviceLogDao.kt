package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.SmartAdviceLog
import com.rudra.lifeledge.data.local.entity.AdviceType
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartAdviceLogDao {
    @Query("SELECT * FROM smart_advice_logs WHERE id = :id")
    suspend fun getSmartAdviceLog(id: Long): SmartAdviceLog?

    @Query("SELECT * FROM smart_advice_logs WHERE date = :date")
    fun getSmartAdviceLogsForDate(date: String): Flow<List<SmartAdviceLog>>

    @Query("SELECT * FROM smart_advice_logs WHERE type = :type ORDER BY date DESC")
    fun getSmartAdviceLogsByType(type: AdviceType): Flow<List<SmartAdviceLog>>

    @Query("SELECT * FROM smart_advice_logs ORDER BY date DESC")
    fun getAllSmartAdviceLogs(): Flow<List<SmartAdviceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartAdviceLog(smartAdviceLog: SmartAdviceLog): Long

    @Update
    suspend fun updateSmartAdviceLog(smartAdviceLog: SmartAdviceLog)

    @Delete
    suspend fun deleteSmartAdviceLog(smartAdviceLog: SmartAdviceLog)
}
