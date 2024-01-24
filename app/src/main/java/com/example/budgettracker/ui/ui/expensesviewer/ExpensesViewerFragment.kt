package com.example.budgettracker.ui.ui.expensesviewer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.example.budgettracker.R
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.databinding.FragmentExpensesViewerBinding
import com.example.budgettracker.ui.MainActivity
import com.example.budgettracker.ui.expensesform.ExpensesForm
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ExpensesViewerFragment : Fragment() {

    private var _binding: FragmentExpensesViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: ExpensesViewerViewModel
    private lateinit var filterViewModel: ExpensesFilterViewModel
    private lateinit var latestExpensesList: List<Expense>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        mainViewModel = ViewModelProvider(this)[ExpensesViewerViewModel::class.java]
        filterViewModel = ViewModelProvider(this)[ExpensesFilterViewModel::class.java]

        _binding = FragmentExpensesViewerBinding.inflate(inflater, container, false)
        styleRecyclerView()

        observeSearchEditText()

        mainViewModel.allExpenses.observe (requireActivity()) {unparsedExpenses ->
            lifecycleScope.launch {
                val parsedExpenses = mainViewModel.parseExpenses(unparsedExpenses)
                reloadRecyclerView(parsedExpenses)
            }
        }

        binding.fabInsertExpense.setOnClickListener {
            val intent = Intent(requireContext(), ExpensesForm::class.java)
            intent.putExtra("Action", "Insert")
            startActivity(intent)
        }

        binding.list.scrollListener = onListScrollListener

//        binding.searchView.registerFilterAction {
//            val modalBottomSheet = ExpensesFilter {filterOptions ->
//                lifecycleScope.launch {
//                    val sortedList = filterViewModel.handleFiltering(mainViewModel, filterOptions)
//                    reloadRecyclerView(sortedList)
//                }
//            }
//
//            modalBottomSheet.show(requireParentFragment().parentFragmentManager, ExpensesFilter.TAG)
//
//        }
        return binding.root
    }

    private fun observeSearchEditText() {
//        binding.searchView.searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                lifecycleScope.launch {
//                    if(s.toString().isNotEmpty()) {
//                        val currentExpenses = mainViewModel.allExpenses.value!!
//                        val matchesList = mutableListOf<Expense>()
//                        for (expense in mainViewModel.parseExpenses(currentExpenses)) {
//                            if(expense.isStringPresent(s.toString())) {
//                                matchesList.add(expense)
//                            }
//                        }
//                        reloadRecyclerView(matchesList)
//                    }
//                    else {
//                        reloadRecyclerView(mainViewModel.parseExpenses(mainViewModel.allExpenses.value!!))
//                    }
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//        })
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
        }

        override fun onListScrolled(scrollDirection: OnListScrollListener.ScrollDirection, distance: Int) {
            if (scrollDirection == OnListScrollListener.ScrollDirection.DOWN &&
                (requireActivity() as MainActivity).isBottomNavBarVisible())
            {
                (requireActivity() as MainActivity).hideBottomNavBar()
                binding.fabInsertExpense.hide()
            }
            else if (scrollDirection == OnListScrollListener.ScrollDirection.UP &&
                !(requireActivity() as MainActivity).isBottomNavBarVisible())
            {
                (requireActivity() as MainActivity).showBottomNavBar()
                binding.fabInsertExpense.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun styleRecyclerView() {
        binding.list.itemLayoutId = R.layout.expensee_viewer_list_item

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        binding.list.swipeListener = onItemSwipeListener

        binding.list.behindSwipedItemLayoutId = R.layout.left_menu_item
        binding.list.behindSwipedItemSecondaryLayoutId = R.layout.right_menu_item

        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<Expense> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: Expense): Boolean {
            if (direction == OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Are you sure you want to delete this entry?")
                    .setNegativeButton("Dismiss") { _, _ ->
                        (binding.list.adapter as ExpensesViewerAdapter).insertItem(position, item)
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            mainViewModel.deleteExpense(item)
                        }
                    }
                    .show()
            }
            else {
                val intent = Intent(requireContext(), ExpensesForm::class.java)
                intent.putExtra("Action", "Edit")
                intent.putExtra("Expense", item)
                startActivity(intent)
            }
            return false
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        // Reload the items whenever this activity is resumed
        refreshRecyclerView()
        super.onResume()
    }

    private fun reloadRecyclerView(expensesList: List<Expense>) {
        if (binding.list.adapter == null) {
            binding.list.adapter = ExpensesViewerAdapter(expensesList)
        }
        (binding.list.adapter as ExpensesViewerAdapter).updateDataSet(expensesList)

        latestExpensesList = expensesList
    }

    private fun refreshRecyclerView() {
        if (binding.list.adapter != null) {
            (binding.list.adapter as ExpensesViewerAdapter).refresh()
        }
    }
}