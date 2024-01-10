package com.example.budgettracker.ui.expensesform

import android.app.DatePickerDialog
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.budgettracker.R
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.ceil
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgettracker.database.categories.Category
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideDrawable
import com.github.razir.progressbutton.showDrawable
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpensesForm : AppCompatActivity() {
    private lateinit var categoryInput: SuggestionEditTextContainer
    private lateinit var subcategoryInput: SuggestionEditTextContainer
    private lateinit var costInput: SuggestionEditTextContainer
    private lateinit var countInput: SuggestionEditTextContainer
    private lateinit var dateInput: SuggestionEditTextContainer
    private lateinit var submitButton: MaterialButton

    private lateinit var viewModel: ExpenseFormViewModel

    private var currentCategoriesList = listOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_form)

        viewModel = ViewModelProvider(this)[ExpenseFormViewModel::class.java]

        populateViewsReferences()
        observeCategories()
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

        bindProgressButton(submitButton)
        submitButton.attachTextChangeAnimator {
            fadeOutMills = 150
            fadeInMills = 150
        }

        submitButton.setOnClickListener {
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

                lifecycleScope.launch {
                    val submitStatus = viewModel.submitExpense(categoryLiteral, subcategoryLiteral, costLiteral, countLiteral, dateLiteral)
                    lateinit var animatedDrawable: Drawable

                    if (submitStatus) {
                        // Clear all inputs
                        categoryInput.editText.text = null
                        subcategoryInput.editText.text = null
                        costInput.editText.text = null
                        countInput.editText.text = null
                        dateInput.editText.text = null
                        subcategoryInput.chipsContainer!!.removeAllViews()

                        // Set focus on first edit text
                        categoryInput.editText.requestFocus()

                        animatedDrawable = ContextCompat.getDrawable(this@ExpensesForm, R.drawable.tick_icon_raw)!!
                        animatedDrawable.setBounds(0, 0, 40, 40)
                    }
                    else {
                        animatedDrawable = ContextCompat.getDrawable(this@ExpensesForm, R.drawable.failed_icon_raw)!!
                        animatedDrawable.setBounds(0, 0, 35, 40)
                    }

                    // Animate the button depending on the submit status
                    submitButton.showDrawable(animatedDrawable)  {
                        buttonTextRes = if(submitStatus) R.string.insert_expense_success else R.string.insert_expense_failed

                        submitButton.backgroundTintList = ContextCompat.getColorStateList(this@ExpensesForm,
                            if(submitStatus) R.color.success else R.color.failed)

                        textMarginPx = 10

                        lifecycleScope.launch {
                            delay(2000)
                            submitButton.hideDrawable(R.string.submit_expense)
                            submitButton.backgroundTintList = ContextCompat.getColorStateList(this@ExpensesForm, R.color.purple_200)
                        }
                    }
                }
            }
        }
    }

    private fun observeCategories() {
        viewModel.allCategories.observe(this) { rawCategories ->
            val parsedCategories = viewModel.parseRawCategories(rawCategories)
            val parsedCategoriesLiterals = parsedCategories.map {it.name}
            currentCategoriesList = parsedCategories
            populateSuggestionContainer(categoryInput, parsedCategoriesLiterals)
        }

        categoryInput.editText.addTextChangedListener {newText ->
            for (category in currentCategoriesList) {
                if (category.name == newText.toString()) {
                    populateSuggestionContainer(subcategoryInput, category.subcategories.map {it.name})
                    break
                }
            }

            if (newText.toString().isEmpty()) {
                subcategoryInput.chipsContainer!!.removeAllViews()
            }
        }
    }
}

data class SuggestionEditTextContainer(
    var editText: TextInputEditText,
    var editTextLayout: TextInputLayout,
    var chipsContainer: LinearLayout?)
