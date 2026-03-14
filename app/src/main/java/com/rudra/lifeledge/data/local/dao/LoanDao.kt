package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoan(id: Long): Loan?

    @Query("SELECT * FROM loans ORDER BY name")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT SUM(remainingAmount) FROM loans")
    fun getTotalRemainingAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)
}
