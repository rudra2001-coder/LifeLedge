package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.EMIPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIPaymentDao {
    @Query("SELECT * FROM emi_payments WHERE id = :id")
    suspend fun getEMIPayment(id: Long): EMIPayment?

    @Query("SELECT * FROM emi_payments WHERE loanId = :loanId ORDER BY date")
    fun getEMIPaymentsForLoan(loanId: Long): Flow<List<EMIPayment>>

    @Query("SELECT * FROM emi_payments WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getEMIPaymentsBetween(startDate: String, endDate: String): Flow<List<EMIPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMIPayment(emiPayment: EMIPayment): Long

    @Update
    suspend fun updateEMIPayment(emiPayment: EMIPayment)

    @Delete
    suspend fun deleteEMIPayment(emiPayment: EMIPayment)
}
