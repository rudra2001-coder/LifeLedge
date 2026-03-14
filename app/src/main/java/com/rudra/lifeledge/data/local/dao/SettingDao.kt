package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSetting(key: String): Setting?

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Delete
    suspend fun deleteSetting(setting: Setting)
}
