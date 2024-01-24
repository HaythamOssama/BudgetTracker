package com.example.budgettracker.ui.ui.expensesviewer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.example.budgettracker.R

class BackdropLayoutToolbarViewLayoutBind @JvmOverloads constructor(context: Context, attribute : AttributeSet? = null, defStyleAttr: Int = 0):
    View(context, attribute, defStyleAttr)
{
    var boundLayoutId: Int = 0
    var populatedChild: View ?= null
    init {
        val typedArray = context.obtainStyledAttributes(attribute, R.styleable.BackdropLayoutToolbarViewLayoutBind)
        boundLayoutId = typedArray.getResourceId(R.styleable.BackdropLayoutToolbarViewLayoutBind_bound_layout,0)

        if (boundLayoutId == 0) {
            throw Exception("Must provide a layout to bind to")
        }

        typedArray.recycle()
    }

}