package com.example.budgettracker.database.databasewrappers

import androidx.room.RoomDatabase
import com.example.budgettracker.database.categories.CategoryDao
import com.example.budgettracker.database.expenses.ExpenseDao
import com.example.budgettracker.database.subcategory.SubcategoryDao

abstract class AppDatabaseBase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun categoryDao(): CategoryDao
}