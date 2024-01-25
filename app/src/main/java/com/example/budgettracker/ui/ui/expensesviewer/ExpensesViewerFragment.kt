package com.example.budgettracker.ui.ui.expensesviewer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
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
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent


class ExpensesViewerFragment : Fragment() {

    private var _binding: FragmentExpensesViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: ExpensesViewerViewModel
    private lateinit var filterViewModel: ExpensesFilterViewModel
    private lateinit var latestExpensesList: List<Expense>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        mainViewModel = ViewModelProvider(this)[ExpensesViewerViewModel::class.java]
        filterViewModel = ViewModelProvider(requireActivity())[ExpensesFilterViewModel::class.java]

        _binding = FragmentExpensesViewerBinding.inflate(inflater, container, false)
        styleRecyclerView()

        observeSearchEditText()
        observeExpenses()
        observeFilterChanges()

        binding.insertButton.setOnClickListener {
            val intent = Intent(requireContext(), ExpensesForm::class.java)
            intent.putExtra("Action", "Insert")
            startActivity(intent)
        }

        binding.filterButton.setOnClickListener {
            val modalBottomSheet = ExpensesFilter()
            modalBottomSheet.show(requireParentFragment().parentFragmentManager, ExpensesFilter.TAG)
        }

        return binding.root
    }

    private fun observeSearchEditText() {
        // clear focus when keyboard is closed
        KeyboardVisibilityEvent.setEventListener(requireActivity()) { isOpen ->
            if (!isOpen) {
                binding.searchButton.clearFocus()
            }
        }
        binding.searchButton.setOnQueryTextListener(object: OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchButton.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText!!.isNotEmpty() && mainViewModel.allExpenses.value != null) {
                    val matchesList = mutableListOf<Expense>()
                    for (expense in latestExpensesList) {
                        if(expense.isStringPresent(newText)) {
                            matchesList.add(expense)
                        }
                    }
                    reloadRecyclerView(matchesList)
                }
                else {
                    reloadRecyclerView()
                }
                return true
            }
        })
    }

    private fun observeExpenses() {
        mainViewModel.allExpenses.observe (requireActivity()) {unparsedExpenses ->
            lifecycleScope.launch {
                val parsedExpenses = mainViewModel.parseExpenses(unparsedExpenses)
                reloadRecyclerView(filterViewModel.handleFiltering(parsedExpenses, filterViewModel.filterOptions.value!!))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun observeFilterChanges() {
        filterViewModel.filterOptions.postValue(FilterOptions())

        filterViewModel.filterOptions.observe (requireActivity()) {
            if (mainViewModel.allExpenses.value != null) {
                reloadRecyclerView()
            }

            // Change the color of the filter icon if non default filtering occurred
            if (it != FilterOptions()) {
                binding.filterButton.setColorFilter(resources.getColor(R.color.failed))
            }
            else {
                binding.filterButton.colorFilter = null
            }

        }
    }

    private fun styleRecyclerView() {
        binding.list.itemLayoutId = R.layout.expensee_viewer_list_item

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

        binding.list.behindSwipedItemLayoutId = R.layout.left_menu_item
        binding.list.behindSwipedItemSecondaryLayoutId = R.layout.right_menu_item

        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
        binding.list.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)

        binding.list.scrollListener = onListScrollListener
        binding.list.swipeListener = onItemSwipeListener
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
        }

        override fun onListScrolled(scrollDirection: OnListScrollListener.ScrollDirection, distance: Int) {
            if (scrollDirection == OnListScrollListener.ScrollDirection.DOWN &&
                (requireActivity() as MainActivity).isBottomNavBarVisible())
            {
                (requireActivity() as MainActivity).hideBottomNavBar()
            }
            else if (scrollDirection == OnListScrollListener.ScrollDirection.UP &&
                !(requireActivity() as MainActivity).isBottomNavBarVisible())
            {
                (requireActivity() as MainActivity).showBottomNavBar()
            }
        }
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

    private fun reloadRecyclerView(expensesList: List<Expense> ?= null) {
        lifecycleScope.launch {
            val newExpenseList = expensesList?:
            filterViewModel.handleFiltering(mainViewModel.parseExpenses(mainViewModel.allExpenses.value!!),
                filterViewModel.filterOptions.value!!)

            if (binding.list.adapter == null) {
                binding.list.adapter = ExpensesViewerAdapter(newExpenseList)
            }
            (binding.list.adapter as ExpensesViewerAdapter).updateDataSet(newExpenseList)

            latestExpensesList = newExpenseList
        }
    }

    private fun refreshRecyclerView() {
        if (binding.list.adapter != null) {
            (binding.list.adapter as ExpensesViewerAdapter).refresh()
        }
    }

    override fun onResume() {
        // Reload the items whenever this activity is resumed
        refreshRecyclerView()
        if (mainViewModel.allExpenses.value != null && mainViewModel.allExpenses.value!!.isNotEmpty()) {
            lifecycleScope.launch {
                reloadRecyclerView()
            }
        }
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}