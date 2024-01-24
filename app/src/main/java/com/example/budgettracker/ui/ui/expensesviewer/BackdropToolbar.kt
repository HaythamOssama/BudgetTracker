package com.example.budgettracker.ui.ui.expensesviewer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.appbar.MaterialToolbar

class BackdropToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet) : MaterialToolbar(context, attrs) {
    var viewLayoutBindList = mutableListOf<BackdropLayoutToolbarViewLayoutBind>()

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewLayoutBindList = getAllChildViews(getChildAt(0)).toMutableList()
    }

    private fun getAllChildViews(parentView: View): List<BackdropLayoutToolbarViewLayoutBind> {
        val allChildViews = mutableListOf<BackdropLayoutToolbarViewLayoutBind>()

        if (parentView is ViewGroup) {
            for (i in 0 until parentView.childCount) {
                val childView = parentView.getChildAt(i)

                if (childView is BackdropLayoutToolbarViewLayoutBind) {
                    // Add the current child view to the list
                    allChildViews.add(childView)
                }

                // Recursively call the function for nested view groups
                allChildViews.addAll(getAllChildViews(childView))
            }
        }

        return allChildViews
    }

}