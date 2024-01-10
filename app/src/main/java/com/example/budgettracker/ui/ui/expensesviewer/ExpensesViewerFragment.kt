package com.example.budgettracker.ui.ui.expensesviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentExpensesViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExpensesViewerFragment : Fragment() {

    private var _binding: FragmentExpensesViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpensesViewerViewModel
    private lateinit var dataSet: MutableList<String>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ExpensesViewerViewModel::class.java]
        _binding = FragmentExpensesViewerBinding.inflate(inflater, container, false)
        styleRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun styleRecyclerView() {
        binding.list.itemLayoutId = R.layout.expensee_viewer_list_item

        // TODO: Remove this when the view model is implemented
        dataSet = mutableListOf("Item 1", "Item 2", "Item 3")
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = ExpensesViewerAdapter(dataSet)
        binding.list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        binding.list.swipeListener = onItemSwipeListener

        binding.list.behindSwipedItemLayoutId = R.layout.left_menu_item
        binding.list.behindSwipedItemSecondaryLayoutId = R.layout.right_menu_item

        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<String> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: String): Boolean {
            if (direction == OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Are you sure you want to delete this entry?")
                    .setNegativeButton("Dismiss") { _, _ ->
                        (binding.list.adapter as ExpensesViewerAdapter).insertItem(position, item)
                    }
                    .setPositiveButton("Yes") { _, _ ->
                    }
                    .show()
            }
            return false
        }
    }

}