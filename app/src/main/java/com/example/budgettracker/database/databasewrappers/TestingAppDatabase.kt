package com.example.budgettracker.database.databasewrappers

import androidx.room.Database
import androidx.room.TypeConverters
import com.example.budgettracker.database.Converters
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.subcategory.Subcategory

@TypeConverters(Converters::class)
@Database(entities = [Category::class, Subcategory::class, Expense::class], version = 2)
abstract class TestingAppDatabase() : AppDatabaseBase() {
}