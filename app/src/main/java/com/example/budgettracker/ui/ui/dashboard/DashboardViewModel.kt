package com.example.budgettracker.ui.ui.dashboard

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.example.budgettracker.database.backup.DatabaseBackup
import com.example.budgettracker.utils.Logger
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DashboardViewModel : ViewModel() {

    @WorkerThread
    suspend fun backupDatabase(): Map<String, Boolean> {
        val backupExecutor = DatabaseBackup(Application())
        return backupExecutor.startBackup()
    }
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = storage.reference

    @WorkerThread
    private suspend fun listDirectories(folderPath: String): List<StorageReference> {
        val folderReference: StorageReference = storageReference.child(folderPath)

        return suspendCoroutine { continuation ->
            folderReference.listAll().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val directoryList = task.result.prefixes
                    continuation.resume(directoryList)
                } else {
                    // Handle the exception
                    val exception = task.exception
                    exception?.printStackTrace()
                    continuation.resume(emptyList()) // Return an empty list in case of an error
                }
            }
        }
    }

    @WorkerThread
    suspend fun monitorRemoteBackupResources(): Pair<String, String> {
        val directoryList = listDirectories("backup/")
        var directoryName = ""
        var downloadUrl = ""

        if (directoryList.isNotEmpty()) {
            val maxDateIndex = findLatestDateIndex(directoryList.map { it.name})
            directoryName = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.UK).parse(directoryList[maxDateIndex].name)!!.toString()
            downloadUrl = directoryList[maxDateIndex].downloadUrl.toString()
        }

        return Pair(directoryName, downloadUrl)
    }

    private fun findLatestDateIndex(dateStrings: List<String>): Int {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.UK)
        val dateObjects = dateStrings.mapNotNull { dateString ->
            try {
                dateFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }

        return dateObjects.indexOf(dateObjects.max())
    }

}