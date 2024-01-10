package com.example.budgettracker.ui.ui.expensesviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgettracker.databinding.FragmentExpensesViewerBinding

class ExpensesViewerFragment : Fragment() {

    private var _binding: FragmentExpensesViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpensesViewerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ExpensesViewerViewModel::class.java]
        _binding = FragmentExpensesViewerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}