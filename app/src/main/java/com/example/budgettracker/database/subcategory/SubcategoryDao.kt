package com.example.budgettracker.database.subcategory

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.utils.VisibleForTestingOnly

@Dao
interface SubcategoryDao {
    @Insert
    suspend fun insert(subcategory: Subcategory): Long

    @Query("SELECT * FROM Subcategories JOIN Categories ON Categories.id = Subcategories.categoryId")
    fun getAllLive(): LiveData<Map<Subcategory, List<Category>>>

    @Query("SELECT * FROM Subcategories where name = :name")
    suspend fun get(name: String): Subcategory

    @Query("SELECT * FROM Subcategories where id = :id")
    suspend fun get(id: Int): Subcategory

    @Query("SELECT * FROM Subcategories where categoryId = :categoryId")
    suspend fun get(categoryId: Long): List<Subcategory>

    @Delete
    suspend fun delete(subcategory: Subcategory): Int

    @VisibleForTestingOnly
    @Query("SELECT * FROM Subcategories")
    fun getAll() : List<Subcategory>

}