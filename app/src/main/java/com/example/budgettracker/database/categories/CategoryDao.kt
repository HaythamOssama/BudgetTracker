package com.example.budgettracker.database.categories

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgettracker.database.subcategory.Subcategory

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category) : Long

    @Query("SELECT * FROM Categories JOIN Subcategories ON Categories.id = Subcategories.categoryId")
    fun getAllLive() : LiveData<Map<Category, List<Subcategory>>>

    @Query("SELECT * FROM Categories where name = :name")
    suspend fun get(name: String): Category?

    @Query("SELECT * FROM Categories where id = :id")
    suspend fun get(id: Long): Category?

    @Delete
    suspend fun delete(category: Category): Int

    @Query("SELECT * FROM Categories")
    suspend fun getAll() : List<Category>
}