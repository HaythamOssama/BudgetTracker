package com.example.budgettracker.ui.expensesform

import android.app.DatePickerDialog
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class ExpensesForm : AppCompatActivity() {
    private lateinit var categoryInput: SuggestionEditTextContainer
    private lateinit var subcategoryInput: SuggestionEditTextContainer
    private lateinit var costInput: SuggestionEditTextContainer
    private lateinit var countInput: SuggestionEditTextContainer
    private lateinit var dateInput: SuggestionEditTextContainer
    private lateinit var submitButton: MaterialButton

    private lateinit var viewModel: ExpenseFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_form)

        viewModel = ViewModelProvider(this)[ExpenseFormViewModel::class.java]

        populateViewsReferences()

        /* TODO Remove these lists */
        val catList = listOf("Category 1", "Category 2", "Category 3", "Category 4", "Category 5",
            "Category 6", "Category 101")

        val subcatList = listOf("Subcategory 1", "Subcategory 2", "Subcategory 3", "Subcategory 4", "Subcategory 5",
            "Subcategory 6", "Subcategory 101")

        populateSuggestionContainer(categoryInput, catList)
        populateSuggestionContainer(subcategoryInput, subcatList)

        populateDatePicker()
        populateSubmitButton()
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

        costInput = SuggestionEditTextContainer(
            findViewById(R.id.costEditText),
            findViewById(R.id.costEditTextLayout),
            null
        )

        countInput = SuggestionEditTextContainer(
            findViewById(R.id.countEditText),
            findViewById(R.id.countEditTextLayout),
            null
        )

        dateInput = SuggestionEditTextContainer(
            findViewById(R.id.dateEditText),
            findViewById(R.id.dateEditTextLayout),
            null
        )

        // Set the next view when next is pressed
        costInput.editText.nextFocusDownId = R.id.countEditText
        countInput.editText.nextFocusDownId = R.id.dateEditText

        submitButton = findViewById(R.id.submitExpenseButton)
    }

    private fun populateSuggestionContainer(suggestionEditTextContainer: SuggestionEditTextContainer,
                                            chipsList: List<String>) {
        populateChipsContainer(chipsList, suggestionEditTextContainer.chipsContainer!!) {
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
            populateChipsContainer(matchList, suggestionEditTextContainer.chipsContainer!!) {
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

    private fun populateDatePicker() {
        val calendar = Calendar.getInstance()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                dateInput.editText.setText(viewModel.getDateFormat().format(calendar.time))
            }

        dateInput.editText.setOnClickListener {
            DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun populateSubmitButton() {
        val verifyEditText: (SuggestionEditTextContainer) -> Boolean = {
            if (it.editText.text.isNullOrEmpty()) {
                it.editTextLayout.error = "Required Field"
                it.editText.requestFocus()
                false
            }
            else {
                it.editTextLayout.error = null
                true
            }
        }

        submitButton.setOnClickListener {
            val verifyThese = listOf(
                { verifyEditText(categoryInput) }, { verifyEditText(subcategoryInput) },
                { verifyEditText(costInput) }, { verifyEditText(countInput) }, { verifyEditText(dateInput) }
            )

            if(verifyThese.all { it() }) {
                val categoryLiteral = categoryInput.editText.text.toString()
                val subcategoryLiteral = subcategoryInput.editText.text.toString()
                val costLiteral = costInput.editText.text.toString()
                val countLiteral = countInput.editText.text.toString()
                val dateLiteral = dateInput.editText.text.toString()

                viewModel.submitExpense(categoryLiteral, subcategoryLiteral, costLiteral, countLiteral, dateLiteral)

                // Clear all inputs
                categoryInput.editText.text = null
                subcategoryInput.editText.text = null
                costInput.editText.text = null
                countInput.editText.text = null
                dateInput.editText.text = null

                // Set focus on first edit text
                categoryInput.editText.requestFocus()
            }

        }
    }
}
data class SuggestionEditTextContainer(
    var editText: TextInputEditText,
    var editTextLayout: TextInputLayout,
    var chipsContainer: LinearLayout?)
