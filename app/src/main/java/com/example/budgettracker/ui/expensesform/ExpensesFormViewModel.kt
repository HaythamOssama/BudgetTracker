package com.example.budgettracker.ui.expensesform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseFormViewModel(app: Application): AndroidViewModel(app) {

    fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    }
}