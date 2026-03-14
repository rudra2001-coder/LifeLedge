package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccount(id: Long): Account?

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name")
    fun getActiveAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY name")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT SUM(balance) FROM accounts WHERE isActive = 1")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT * FROM accounts WHERE type = :type AND isActive = 1")
    fun getAccountsByType(type: AccountType): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)
}
