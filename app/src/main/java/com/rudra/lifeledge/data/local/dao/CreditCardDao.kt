package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCreditCard(id: Long): CreditCard?

    @Query("SELECT * FROM credit_cards ORDER BY name")
    fun getAllCreditCards(): Flow<List<CreditCard>>

    @Query("SELECT SUM(availableCredit) FROM credit_cards")
    fun getTotalAvailableCredit(): Flow<Double?>

    @Query("SELECT SUM(creditLimit) FROM credit_cards")
    fun getTotalCreditLimit(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(creditCard: CreditCard): Long

    @Update
    suspend fun updateCreditCard(creditCard: CreditCard)

    @Delete
    suspend fun deleteCreditCard(creditCard: CreditCard)
}
