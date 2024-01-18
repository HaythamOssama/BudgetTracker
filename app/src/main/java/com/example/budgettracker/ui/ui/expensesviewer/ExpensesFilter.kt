package com.example.budgettracker.ui.ui.expensesviewer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.budgettracker.R
import com.example.budgettracker.utils.Logger
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.io.Serializable

class ExpensesFilter : AppCompatActivity() {

    private lateinit var applyFilterButton: MaterialButton
    private lateinit var sortByChipGroup: ChipGroup
    private lateinit var sortModeChipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_filter)

        applyFilterButton = findViewById(R.id.applyFilterButton)
        sortByChipGroup = findViewById(R.id.sortByChipGroup)
        sortModeChipGroup = findViewById(R.id.sortModeChipGroup)

        applyFilterButton.setOnClickListener {
            val intent = Intent(this, ExpensesViewerFragment::class.java)

            val selectedSortBy = parseSelectedSortChip(sortByChipGroup.checkedChipId)
            val selectedSortMode = parseSelectedSortModeChip(sortModeChipGroup.checkedChipId)
            val filterOptions = FilterOptions(selectedSortBy, selectedSortMode)

            intent.putExtra("FilterOptions", filterOptions)
            setResult(Activity.RESULT_OK, intent)

            Logger.logDebug("Put: $filterOptions")

            finish()
//            startActivity(intent)
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
    val sortMode: FilterOptionsSortMode
) : Serializable
{
    override fun toString(): String{
        return "Sort By: ${sortBy.name} - Sort Mode: ${sortMode.name}"
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