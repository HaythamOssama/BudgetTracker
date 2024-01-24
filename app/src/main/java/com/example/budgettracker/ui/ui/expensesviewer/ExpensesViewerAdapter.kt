package com.example.budgettracker.ui.ui.expensesviewer

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.example.budgettracker.R
import com.example.budgettracker.database.expenses.Expense

class ExpensesViewerAdapter(dataSet: List<Expense> = emptyList()) : DragDropSwipeAdapter<Expense, ExpensesViewerAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val subcategoryTextView: TextView = itemView.findViewById(R.id.subcategoryTextView)
        val costTextView: TextView = itemView.findViewById(R.id.costTextView)
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        val monthTextView: TextView = itemView.findViewById(R.id.monthTextView)
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(item: Expense, viewHolder: ViewHolder, position: Int) {
        // Here we update the contents of the view holder's views to reflect the item's data
        viewHolder.categoryTextView.text = item.subcategory!!.category!!.name
        viewHolder.subcategoryTextView.text = item.subcategory!!.name
        viewHolder.costTextView.text = formatDecimal(item.cost)
        viewHolder.countTextView.text = "x${formatDecimal(item.count)}"
        viewHolder.dayTextView.text = item.date.split(" ")[0]
        viewHolder.monthTextView.text = item.date.split(" ")[1].substring(0, 3).uppercase()
        viewHolder.yearTextView.text = item.date.split(" ")[2]
        viewHolder.categoryImage.setBackgroundResource(R.drawable.grocery)
    }

    private fun formatDecimal(number: Double): String {
        return if (number % 1 == 0.0) {
            String.format("%.0f", number)
        } else {
            number.toString()
        }
    }

    override fun getViewToTouchToStartDraggingItem(item: Expense, viewHolder: ViewHolder, position: Int): View? {
        // We return the view holder's view on which the user has to touch to drag the item
        return null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(newList: List<Expense>) {
        super.dataSet = newList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }
}
