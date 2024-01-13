package com.example.budgettracker.ui.ui.expensesviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.budgettracker.database.DatabaseRepo
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.subcategory.Subcategory

class ExpensesViewerViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DatabaseRepo(app)
    val allExpenses = repo.allExpenses

    suspend fun parseExpenses(unparsedExpenses: Map<Expense, Map<Subcategory, Category>>): List<Expense>{
        return repo.getParsedExpenses(unparsedExpenses)
    }

    suspend fun deleteExpense(expense: Expense): Boolean {
        return repo.deleteExpense(expense)
    }
}