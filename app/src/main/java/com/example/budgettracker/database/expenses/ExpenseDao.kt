package com.example.budgettracker.database.expenses

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.subcategory.Subcategory

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM EXPENSES LEFT JOIN Subcategories ON Subcategories.id = Expenses.subcategoryId INNER JOIN Categories ON Categories.id = Subcategories.categoryId")
    fun getAllLive(): LiveData<Map<Expense, Map<Subcategory, Category>>>

    @Query("SELECT * FROM EXPENSES INNER JOIN Subcategories ON Subcategories.id = Expenses.subcategoryId INNER JOIN Categories ON Categories.id = Subcategories.categoryId where Expenses.id = :id")
    suspend fun getExpenseMappedById(id: Int): Map<Expense, Map<Subcategory, Category>>

    @Update
    suspend fun update(expense: Expense): Int

    @Delete
    suspend fun delete(expense: Expense): Int

    @Query("SELECT * FROM Expenses")
    suspend fun getAll(): List<Expense>
}