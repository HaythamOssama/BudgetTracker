package com.example.budgettracker.ui.ui.expensesviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.utils.getGlobalSimpleDateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpensesFilterViewModel: ViewModel() {

    val filterOptions = MutableLiveData(
        FilterOptions(
            sortBy = FilterOptionsSortBy.SORT_BY_DATE,
            sortMode = FilterOptionsSortMode.SORT_DESCENDING,
            isDataRangePresent = false,
            dateRange = Pair(Date(), Date())
        )
    )

    fun handleFiltering(expensesToBeFiltered: List<Expense>,
                        filterOptions: FilterOptions): List<Expense>
    {
        var currentExpenses = expensesToBeFiltered
        if (filterOptions.isDataRangePresent) {
            currentExpenses = currentExpenses.filter { getGlobalSimpleDateFormat().parse(it.date)!! in filterOptions.dateRange.first..filterOptions.dateRange.second }
        }

        val sortedList = when (filterOptions.sortBy) {
            FilterOptionsSortBy.SORT_BY_COST -> sortListBy(currentExpenses, filterOptions.sortMode) { it.cost }
            FilterOptionsSortBy.SORT_BY_COUNT -> sortListBy(currentExpenses, filterOptions.sortMode) { it.count }
            FilterOptionsSortBy.SORT_BY_DATE -> sortListByDate(currentExpenses, filterOptions.sortMode)
            else -> currentExpenses
        }

        return sortedList
    }

    private inline fun <T : Comparable<T>> sortListBy(
        list: List<Expense>,
        sortMode: FilterOptionsSortMode,
        crossinline selector: (Expense) -> T
    ): List<Expense> {
        return if (sortMode == FilterOptionsSortMode.SORT_ASCENDING) {
            list.sortedBy(selector)
        } else {
            list.sortedByDescending(selector)
        }
    }

    private fun sortListByDate(
        list: List<Expense>,
        sortMode: FilterOptionsSortMode
    ): List<Expense> {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        return if (sortMode == FilterOptionsSortMode.SORT_ASCENDING) {
            list.sortedBy { dateFormat.parse(it.date) }
        } else {
            list.sortedByDescending { dateFormat.parse(it.date) }
        }
    }
}