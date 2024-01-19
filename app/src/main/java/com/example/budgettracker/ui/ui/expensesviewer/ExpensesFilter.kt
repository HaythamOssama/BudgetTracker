package com.example.budgettracker.ui.ui.expensesviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.budgettracker.R
import com.example.budgettracker.utils.getGlobalSimpleDateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable
import java.util.Date

class ExpensesFilter(private val onApplyAction: (FilterOptions) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var applyFilterButton: MaterialButton
    private lateinit var sortByChipGroup: ChipGroup
    private lateinit var sortModeChipGroup: ChipGroup
    private lateinit var dateRangeEditText: TextInputEditText
    private var startDate = Date()
    private var endDate = Date()
    private var isDataRangePresent = false
    private lateinit var binding: View

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = inflater.inflate(R.layout.activity_expenses_filter, container, false)

        populateViewReferences()
        populateDataRangePicker()
        populateApplyFilterAction()

        return binding
    }

    private fun populateViewReferences() {
        applyFilterButton = binding.findViewById(R.id.applyFilterButton)
        sortByChipGroup = binding.findViewById(R.id.sortByChipGroup)
        sortModeChipGroup = binding.findViewById(R.id.sortModeChipGroup)
        dateRangeEditText = binding.findViewById(R.id.dateRangeEditText)
    }

    @SuppressLint("SetTextI18n")
    private fun populateDataRangePicker() {
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

            dateRangePicker.show(parentFragmentManager, "Filter Date Range")

            dateRangePicker.addOnPositiveButtonClickListener {
                isDataRangePresent = true
                startDate = Date(it.first)
                endDate = Date(it.second)
                val beginDate = getGlobalSimpleDateFormat().format(Date(it.first))
                val endDate = getGlobalSimpleDateFormat().format(Date(it.second))
                dateRangeEditText.setText("$beginDate -> $endDate")
            }
        }
    }

    private fun populateApplyFilterAction() {
        applyFilterButton.setOnClickListener {
            val selectedSortBy = parseSelectedSortChip(sortByChipGroup.checkedChipId)
            val selectedSortMode = parseSelectedSortModeChip(sortModeChipGroup.checkedChipId)
            val dateRange = Pair(startDate, endDate)
            val filterOptions = FilterOptions(selectedSortBy, selectedSortMode, isDataRangePresent, dateRange)
            onApplyAction(filterOptions)
            dismiss()
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

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
}

data class FilterOptions(
    val sortBy: FilterOptionsSortBy,
    val sortMode: FilterOptionsSortMode,
    val isDataRangePresent: Boolean,
    val dateRange: Pair<Date, Date>
) : Serializable
{
    override fun toString(): String{
        return "Sort By: ${sortBy.name} - Sort Mode: ${sortMode.name} - " +
                "Is Data Range Present? ${isDataRangePresent} " +
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