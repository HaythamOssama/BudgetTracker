package com.example.budgettracker.ui.expensesform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseFormViewModel(app: Application): AndroidViewModel(app) {

    fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("dd MMMM yyyy", Locale.UK)
    }

    fun submitExpense(inputCategory: String, inputSubcategory: String, inputCost: String,
                      inputCount: String, inputDate: String): Boolean
    {
        return false
    }
}