package com.example.budgettracker.database.databasewrappers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.subcategory.Subcategory

@Database(entities = [Category::class, Subcategory::class, Expense::class], version = 2)
abstract class AppDatabase: AppDatabaseBase() {
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private const val databaseName = "AppDatabase"

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    databaseName
                )
                    .fallbackToDestructiveMigration().build()
            }
            return instance!!
        }
    }
}