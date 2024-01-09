package com.example.budgettracker.ui.expensesform

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.budgettracker.R
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.ceil
import androidx.core.widget.addTextChangedListener

class ExpensesForm : AppCompatActivity() {
    private lateinit var categoryInput: SuggestionEditTextContainer
    private lateinit var subcategoryInput: SuggestionEditTextContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_form)
        populateViewsReferences()

        /* TODO Remove these lists */
        val catList = listOf("Category 1", "Category 2", "Category 3", "Category 4", "Category 5",
            "Category 6", "Category 101")

        val subcatList = listOf("Subcategory 1", "Subcategory 2", "Subcategory 3", "Subcategory 4", "Subcategory 5",
            "Subcategory 6", "Subcategory 101")

        populateSuggestionContainer(categoryInput, catList)
        populateSuggestionContainer(subcategoryInput, subcatList)
    }

    private fun populateViewsReferences() {
        categoryInput = SuggestionEditTextContainer(
            findViewById(R.id.categoryEditText),
            findViewById(R.id.categoryEditTextLayout),
            findViewById(R.id.categoryChipsContainer)
        )

        subcategoryInput = SuggestionEditTextContainer(
            findViewById(R.id.subcategoryEditText),
            findViewById(R.id.subcategoryEditTextLayout),
            findViewById(R.id.subcategoryChipsContainer)
        )
    }

    private fun populateSuggestionContainer(suggestionEditTextContainer: SuggestionEditTextContainer,
                                            chipsList: List<String>) {
        populateChipsContainer(chipsList, suggestionEditTextContainer.chipsContainer) {
            suggestionEditTextContainer.editText.setText(it)
            suggestionEditTextContainer.editText.setSelection(suggestionEditTextContainer.editText.length())
        }

        suggestionEditTextContainer.editText.addTextChangedListener { s ->
            val matchList = mutableListOf<String>()
            for (item in chipsList) {
                if (item.contains(s.toString(), ignoreCase = true)) {
                    matchList.add(item)
                }
            }
            populateChipsContainer(matchList, suggestionEditTextContainer.chipsContainer) {
                suggestionEditTextContainer.editText.setText(it)
                suggestionEditTextContainer.editText.setSelection(suggestionEditTextContainer.editText.length())
            }
        }

    }

    private fun populateChipsContainer(itemsNames: List<String>,
                                       chipsContainer: LinearLayout,
                                       onClickAction: (String) -> Unit) {
        var chipsPerRow = 7
        val numRows = ceil(itemsNames.size.toDouble() / chipsPerRow).toInt()
        var itemsNamesTracker = 0

        chipsContainer.removeAllViews()

        for (i in 0 until numRows) {
            val rowLinearLayout = LinearLayout(this)

            if((itemsNames.size - itemsNamesTracker) < chipsPerRow) {
                chipsPerRow = itemsNames.size - itemsNamesTracker
            }

            for (j in 0 until chipsPerRow) {
                val chip = Chip(this)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 25
                chip.layoutParams = params
                chip.text = itemsNames[itemsNamesTracker]
                chip.setOnClickListener { onClickAction(chip.text.toString()) }
                rowLinearLayout.addView(chip)
                itemsNamesTracker++
            }

            rowLinearLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            rowLinearLayout.orientation = LinearLayout.HORIZONTAL
            chipsContainer.addView(rowLinearLayout)
        }

    }
}
data class SuggestionEditTextContainer(
    var editText: TextInputEditText,
    var editTextLayout: TextInputLayout,
    var chipsContainer: LinearLayout)
