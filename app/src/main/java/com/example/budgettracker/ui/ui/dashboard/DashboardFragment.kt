package com.example.budgettracker.ui.ui.dashboard

import android.animation.Animator
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentDashboardBinding
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        bindProgressButton(binding.backupDatabaseButton)
        binding.backupDatabaseButton.attachTextChangeAnimator {
            fadeOutMills = 150
            fadeInMills = 150
        }

        binding.backupDatabaseButton.setOnClickListener {
            lifecycleScope.launch {
                val originalColor = binding.backupDatabaseButton.backgroundTintList
                val originalText = binding.backupDatabaseButton.text
                showProgressRight(binding.backupDatabaseButton)
                val statusPerTable = viewModel.backupDatabase()
                val isSuccessful = statusPerTable.values.all { it }
                binding.backupDatabaseButton.isClickable = true
                if (isSuccessful) {
                    binding.backupDatabaseButton.hideProgress(R.string.backup_success)
                    binding.backupDatabaseButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.success)
                }
                else {
                    var errorMessage = ""
                    for ((tableName, status) in statusPerTable) {
                        if (!status) {
                            errorMessage += "[$tableName] "
                        }
                    }
                    errorMessage += "failed to be backed up"
                    binding.backupDatabaseButton.hideProgress(errorMessage)
                    binding.backupDatabaseButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.failed)
                }
                delay(3000)
                setTextWithSmoothAnimation(binding.backupDatabaseButton, originalText.toString(), originalColor!!)


            }
        }

        return binding.root
    }
    private fun setTextWithSmoothAnimation(button: Button, message: String, background: ColorStateList) {
        button.animate().setDuration(300).setListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                button.text = message
                button.backgroundTintList = background
                button.animate().setListener(null).setDuration(300).alpha(1f)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        }).alpha(0f)
    }

    private fun showProgressRight(button: Button) {
        button.showProgress {
            buttonText = "Backing up is in progress"
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.edit)
        }
        button.isClickable = false
    }

    suspend fun populateBackupStatus(latestBackup: String, downloadUrl: String) {
        binding.backupStatusTextView.text = latestBackup
        binding.backupStatusTextView.setOnClickListener {
            val uri = Uri.parse(downloadUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val backupStatus = viewModel.monitorRemoteBackupResources()
            populateBackupStatus(backupStatus.first, backupStatus.second)
//            binding.backupStatusTextView.text = viewModel.monitorRemoteBackupResources()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}