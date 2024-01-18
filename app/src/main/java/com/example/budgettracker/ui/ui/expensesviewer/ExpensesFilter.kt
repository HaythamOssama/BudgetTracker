package com.example.budgettracker.ui.ui.expensesviewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.budgettracker.R
import com.example.budgettracker.utils.getGlobalSimpleDateFormat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable
import java.util.Date

class ExpensesFilter : AppCompatActivity() {

    private lateinit var applyFilterButton: MaterialButton
    private lateinit var sortByChipGroup: ChipGroup
    private lateinit var sortModeChipGroup: ChipGroup
    private lateinit var dateRangeEditText: TextInputEditText
    private var startDate = Date()
    private var endDate = Date()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_filter)

        applyFilterButton = findViewById(R.id.applyFilterButton)
        sortByChipGroup = findViewById(R.id.sortByChipGroup)
        sortModeChipGroup = findViewById(R.id.sortModeChipGroup)
        dateRangeEditText = findViewById(R.id.dateRangeEditText)

        applyFilterButton.setOnClickListener {
            val intent = Intent(this, ExpensesViewerFragment::class.java)

            val selectedSortBy = parseSelectedSortChip(sortByChipGroup.checkedChipId)
            val selectedSortMode = parseSelectedSortModeChip(sortModeChipGroup.checkedChipId)
            val dateRange = Pair(startDate, endDate)
            val filterOptions = FilterOptions(selectedSortBy, selectedSortMode, dateRange)

            intent.putExtra("FilterOptions", filterOptions)
            setResult(Activity.RESULT_OK, intent)

            finish()
        }

        dateRangeEditText.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                        androidx.core.util.Pair(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .build()

            dateRangePicker.show(supportFragmentManager, "Filter Date Range")

            dateRangePicker.addOnPositiveButtonClickListener {
                startDate = Date(it.first)
                endDate = Date(it.second)
                val beginDate = getGlobalSimpleDateFormat().format(Date(it.first))
                val endDate = getGlobalSimpleDateFormat().format(Date(it.second))
                dateRangeEditText.setText("$beginDate -> $endDate")
            }
        }
    }

    private fun parseSelectedSortChip(id: Int): FilterOptionsSortBy {
        var sortBy: FilterOptionsSortBy = FilterOptionsSortBy.SORT_BY_NONE

        when (id) {
            View.NO_ID -> {
                sortBy = FilterOptionsSortBy.SORT_BY_NONE
            }
            R.id.dateChip -> {
                sortBy = FilterOptionsSortBy.SORT_BY_DATE
            }
            R.id.costChip -> {
                sortBy = FilterOptionsSortBy.SORT_BY_COST
            }
            R.id.countChip -> {
                sortBy = FilterOptionsSortBy.SORT_BY_COUNT
            }
        }

        return sortBy
    }

    private fun parseSelectedSortModeChip(id: Int): FilterOptionsSortMode {
        var sortByMode: FilterOptionsSortMode = FilterOptionsSortMode.SORT_ASCENDING

        when (id) {
            R.id.ascendingChip -> {
                sortByMode = FilterOptionsSortMode.SORT_ASCENDING
            }
            R.id.descendingChip -> {
                sortByMode = FilterOptionsSortMode.SORT_DESCENDING
            }
        }

        return sortByMode
    }

}

data class FilterOptions(
    val sortBy: FilterOptionsSortBy,
    val sortMode: FilterOptionsSortMode,
    val dateRange: Pair<Date, Date>
) : Serializable
{
    override fun toString(): String{
        return "Sort By: ${sortBy.name} - Sort Mode: ${sortMode.name} - " +
                "Date Range: ${getGlobalSimpleDateFormat().format(dateRange.first)} -> ${getGlobalSimpleDateFormat().format(dateRange.second)}"
    }
}

enum class FilterOptionsSortBy {
    SORT_BY_NONE,
    SORT_BY_DATE,
    SORT_BY_COST,
    SORT_BY_COUNT,
}

enum class FilterOptionsSortMode {
    SORT_ASCENDING,
    SORT_DESCENDING,
}