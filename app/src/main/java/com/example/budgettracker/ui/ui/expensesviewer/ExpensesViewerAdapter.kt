package com.example.budgettracker.ui.ui.expensesviewer

import android.view.View
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.example.budgettracker.R

class ExpensesViewerAdapter(dataSet: List<String> = emptyList()) : DragDropSwipeAdapter<String, ExpensesViewerAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val subcategoryTextView: TextView = itemView.findViewById(R.id.subcategoryTextView)
        val costTextView: TextView = itemView.findViewById(R.id.costTextView)
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun onBindViewHolder(item: String, viewHolder: ViewHolder, position: Int) {
        // Here we update the contents of the view holder's views to reflect the item's data
        viewHolder.categoryTextView.text = item
        viewHolder.subcategoryTextView.text = item
        viewHolder.costTextView.text = item
        viewHolder.countTextView.text = item
    }

    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: ViewHolder, position: Int): View? {
        // We return the view holder's view on which the user has to touch to drag the item
        return null
    }
}
