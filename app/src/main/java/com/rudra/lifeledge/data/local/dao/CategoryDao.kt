package com.rudra.lifeledge.data.local.dao

import androidx.room.*
import com.rudra.lifeledge.data.local.entity.Category
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): Category?

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name")
    fun getActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND isActive = 1 ORDER BY name")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isActive = 1")
    fun getSubcategories(parentId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
