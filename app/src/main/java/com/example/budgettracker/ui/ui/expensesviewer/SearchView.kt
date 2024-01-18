package com.example.budgettracker.ui.ui.expensesviewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.budgettracker.R


class SearchView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs)
{
    var searchEditText: EditText
    private var openSearchButton: View
    private var closeSearchButton: View
    private var searchOpenView: RelativeLayout
    private var filterButton: View

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search, this, true)
        searchEditText = findViewById(R.id.search_input_text)
        openSearchButton = findViewById(R.id.open_search_button)
        closeSearchButton = findViewById(R.id.close_search_button)
        searchOpenView = findViewById(R.id.search_open_view)
        filterButton = findViewById(R.id.filter_button)

        openSearchButton.setOnClickListener { openSearch() }
        closeSearchButton.setOnClickListener { closeSearch() }
    }

    fun registerFilterAction(onFilterClicked: () -> Unit) {
        filterButton.setOnClickListener {
            onFilterClicked()
        }
    }

    private fun openSearch() {
        searchEditText.setText("")
        searchOpenView.visibility = View.VISIBLE
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            searchOpenView,
            (openSearchButton.right + openSearchButton.left) / 2,
            (openSearchButton.top + openSearchButton.bottom) / 2,
            0f, width.toFloat()
        )
        circularReveal.duration = 300
        circularReveal.start()
        openSearchButton.visibility = View.INVISIBLE
        filterButton.visibility = View.INVISIBLE
    }

    private fun closeSearch() {
        val circularConceal = ViewAnimationUtils.createCircularReveal(
            searchOpenView,
            (openSearchButton.right + openSearchButton.left) / 2,
            (openSearchButton.top + openSearchButton.bottom) / 2,
            width.toFloat(), 0f
        )

        circularConceal.duration = 300
        circularConceal.start()
        circularConceal.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: android.animation.Animator) = Unit
            override fun onAnimationCancel(animation: android.animation.Animator) = Unit
            override fun onAnimationStart(animation: android.animation.Animator) = Unit
            override fun onAnimationEnd(animation: android.animation.Animator) {
                searchOpenView.visibility = View.INVISIBLE
                searchEditText.setText("")
                circularConceal.removeAllListeners()
                openSearchButton.visibility = View.VISIBLE
                filterButton.visibility = View.VISIBLE
            }
        })

    }

}
