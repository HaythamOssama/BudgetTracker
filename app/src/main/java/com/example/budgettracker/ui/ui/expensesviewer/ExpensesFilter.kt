package com.example.budgettracker.ui.ui.expensesviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.budgettracker.R
import com.example.budgettracker.utils.getGlobalSimpleDateFormat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable
import java.util.Date

class ExpensesFilter : BottomSheetDialogFragment() {

    private lateinit var applyFilterButton: MaterialButton
    private lateinit var resetFilterButton: MaterialButton
    private lateinit var sortByChipGroup: ChipGroup
    private lateinit var sortModeChipGroup: MaterialButtonToggleGroup
    private lateinit var dateRangeEditText: TextInputEditText
    private lateinit var categoriesChipGroup: ChipGroup
    private var startDate = Date()
    private var endDate = Date()
    private var isDataRangePresent = false
    private lateinit var binding: View
    private lateinit var viewModel: ExpensesFilterViewModel

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflate(R.layout.activity_expenses_filter, container, false)
        viewModel = ViewModelProvider(requireActivity())[ExpensesFilterViewModel::class.java]

        populateViewReferences()
        populateDataRangePicker()
        populateApplyFilterAction()

        viewModel.filterOptions.observe(requireActivity()) {
            applyFilterOptions(it)
        }

        resetFilterButton.setOnClickListener {
            viewModel.filterOptions.postValue(
                FilterOptions(
                    sortBy = FilterOptionsSortBy.SORT_BY_DATE,
                    sortMode = FilterOptionsSortMode.SORT_DESCENDING,
                )
            )
            dismiss()
        }

        viewModel.allCategories.observe(requireActivity()) {
            val allCategories = it.keys.toList().map {category -> category.name}
            populateChipsContainer(allCategories, categoriesChipGroup)
            if (viewModel.filterOptions.value != null) {
                applySortByCategory(viewModel.filterOptions.value!!.sortByCategory)
            }
        }

        return binding
    }

    private fun applyFilterOptions(filterOptions: FilterOptions) {
        applySortMode(filterOptions.sortMode)
        applySortBy(filterOptions.sortBy)
        if (filterOptions.isDataRangePresent) {
            applyDateRange(filterOptions.dateRange)
        }
    }

    private fun applySortMode(sortMode: FilterOptionsSortMode) {
        if (sortMode == FilterOptionsSortMode.SORT_ASCENDING) {
            binding.findViewById<MaterialButton>(R.id.ascendingChip).isChecked = true
        }
        else if (sortMode == FilterOptionsSortMode.SORT_DESCENDING) {
            binding.findViewById<MaterialButton>(R.id.descendingChip).isChecked = true
        }
    }

    private fun applySortBy(sortBy: FilterOptionsSortBy) {
        when (sortBy) {
            FilterOptionsSortBy.SORT_BY_DATE -> {
                binding.findViewById<Chip>(R.id.dateChip).isChecked = true
            }
            FilterOptionsSortBy.SORT_BY_COST -> {
                binding.findViewById<Chip>(R.id.costChip).isChecked = true
            }
            FilterOptionsSortBy.SORT_BY_COUNT -> {
                binding.findViewById<Chip>(R.id.countChip).isChecked = true
            }
            else -> {}
        }
    }

    @SuppressLint("SetTextI18n")
    private fun applyDateRange(dateRange: Pair<Date, Date>) {
        dateRangeEditText.setText(getGlobalSimpleDateFormat().format(dateRange.first) + " -> " + getGlobalSimpleDateFormat().format(dateRange.second))
    }

    private fun applySortByCategory(sortByCategory: String) {
        for (i in 0 until categoriesChipGroup.childCount) {
            if ((categoriesChipGroup.getChildAt(i) as Chip).text == sortByCategory) {
                categoriesChipGroup.check(categoriesChipGroup.getChildAt(i).id)
                break
            }
        }
    }

    private fun populateViewReferences() {
        applyFilterButton = binding.findViewById(R.id.applyFilterButton)
        resetFilterButton = binding.findViewById(R.id.resetFilterButton)
        sortByChipGroup = binding.findViewById(R.id.sortByChipGroup)
        sortModeChipGroup = binding.findViewById(R.id.sortModeChipGroup)
        dateRangeEditText = binding.findViewById(R.id.dateRangeEditText)
        categoriesChipGroup = binding.findViewById(R.id.categoryChipsContainer)
    }

    private fun populateChipsContainer(itemsNames: List<String>,
                                       chipsContainer: ChipGroup) {
        for (item in itemsNames) {
            val chip = layoutInflater.inflate(R.layout.custom_chip, chipsContainer, false) as Chip
            chip.text = item
            chipsContainer.addView(chip)
        }
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
            val selectedSortMode = parseSelectedSortModeChip(sortModeChipGroup.checkedButtonId)
            val dateRange = Pair(startDate, endDate)
            var selectedCategory = ""
            if (categoriesChipGroup.checkedChipId != View.NO_ID) {
                selectedCategory = binding.findViewById<Chip>(categoriesChipGroup.checkedChipId).text.toString()
            }
            viewModel.filterOptions.postValue(FilterOptions(selectedSortBy, selectedSortMode,
                isDataRangePresent, dateRange, selectedCategory))
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
    val sortBy: FilterOptionsSortBy = FilterOptionsSortBy.SORT_BY_DATE,
    val sortMode: FilterOptionsSortMode = FilterOptionsSortMode.SORT_DESCENDING,
    val isDataRangePresent: Boolean = false,
    val dateRange: Pair<Date, Date> = Pair(Date(), Date()),
    val sortByCategory: String = "",
) : Serializable
{
    override fun toString(): String{
        return "Sort By: ${sortBy.name} - Sort Mode: ${sortMode.name} - " +
                "Is Data Range Present? ${isDataRangePresent} " +
                "Date Range: ${getGlobalSimpleDateFormat().format(dateRange.first)} -> ${getGlobalSimpleDateFormat().format(dateRange.second)} " +
                "Sort By Category: $sortByCategory"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterOptions

        // Compare member variables
        if (sortBy != other.sortBy) return false
        if (sortMode != other.sortMode) return false
        if (isDataRangePresent != other.isDataRangePresent) return false
        if (dateRange.first.toString() != other.dateRange.first.toString()) return false
        if (dateRange.second.toString() != other.dateRange.second.toString()) return false
        if (sortByCategory != other.sortByCategory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sortBy.hashCode()
        result = 31 * result + sortMode.hashCode()
        result = 31 * result + isDataRangePresent.hashCode()
        result = 31 * result + dateRange.hashCode()
        result = 31 * result + sortByCategory.hashCode()
        return result
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