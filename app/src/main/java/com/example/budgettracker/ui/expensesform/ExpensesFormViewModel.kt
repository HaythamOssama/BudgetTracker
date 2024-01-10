package com.example.budgettracker.ui.expensesform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.budgettracker.database.DatabaseRepo
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.subcategory.Subcategory
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseFormViewModel(app: Application): AndroidViewModel(app) {

    private val repo = DatabaseRepo(app)
    val allCategories = repo.allCategories

    fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("dd MMMM yyyy", Locale.UK)
    }

    suspend fun submitExpense(inputCategory: String, inputSubcategory: String, inputCost: String,
                      inputCount: String, inputDate: String): Boolean
    {
        val subcategory = repo.getSubcategoryByName(inputSubcategory)
        // If subcategory exists, the expense can be inserted directly.
        val subcategoryId = if(subcategory != null) {
            subcategory.id
        }
        else {
            // Subcategory does not exist, needs to be inserted first
            val category = repo.getCategoryByName(inputCategory)
            val categoryId = if (category == null) {
                // This is a new category as well. Create a new category.
                val newCategory = Category(name = inputCategory)
                repo.insertCategory(newCategory)
            } else {
                category.id.toLong()
            }

            val newSubcategory = Subcategory(name = inputSubcategory, categoryId = categoryId)
            repo.insertSubcategory(newSubcategory)
        }

        val expense = Expense(subcategoryId = subcategoryId.toLong(), cost = inputCost.toDouble(),
            count = inputCount.toDouble(), date = inputDate)

        return repo.insertExpense(expense)
    }

    fun parseRawCategories(rawCategories: Map<Category, List<Subcategory>>): List<Category>{
        return repo.getParsedCategories(rawCategories)
    }
}