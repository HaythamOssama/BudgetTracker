package com.example.budgettracker.database.backup

import android.app.Application
import com.example.budgettracker.database.DatabaseRepo
import com.example.budgettracker.utils.Logger
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DatabaseBackup(app: Application) {

    private val repo = DatabaseRepo(app)

    suspend fun startBackup(): Map<String, Boolean> {
        val statusPerTable = mutableMapOf<String, Boolean>()
        val databaseJson = convertDatabaseIntoJson()
        val rootDirectory = SimpleDateFormat("dd MMMM yyyy hh:mm:ss", Locale.UK).format(Date())

        for ((tableName, tableContent) in databaseJson) {
            if (tableContent.isNotEmpty()) {
                try {
                    statusPerTable[tableContent] = backupJsonData(tableName, tableContent, rootDirectory)
                } catch (e: Exception) {
                    // Handle exceptions
                    Logger.logError("Exception during backup: $e")
                    statusPerTable[tableContent] = false
                }
            }
        }

        return statusPerTable
    }

    @Throws(java.lang.Exception::class)
    private suspend fun backupJsonData(jsonFileName: String, jsonData: String, rootDirectoryName: String): Boolean {
        val storageRef = FirebaseStorage.getInstance().reference
        val backupFolderRef = storageRef.child("backup/$rootDirectoryName")

        // Use a unique filename based on the current timestamp
        val fileName = "$jsonFileName.json"
        val jsonFileRef: StorageReference = backupFolderRef.child(fileName)

        val uploadTask: UploadTask = jsonFileRef.putBytes(jsonData.toByteArray())
        uploadTask.await() // Wait for the upload to complete

        // Check if the upload was successful
        if (uploadTask.isSuccessful) {
            // Get the download URL
            val downloadUrl: String = jsonFileRef.downloadUrl.await().toString()
            Logger.logDebug("Successful upload -> $downloadUrl")
        } else {
            // Handle the failure
            Logger.logError("Failed to upload. Status: ${uploadTask.exception?.message}")
        }

        return uploadTask.isSuccessful
    }

    private suspend fun convertDatabaseIntoJson(): Map<String, String> {
        val databaseJson = mutableMapOf<String, String>()
        databaseJson["Categories"] = Gson().toJson(repo.getAllCategories())
        databaseJson["Subcategories"] = Gson().toJson(repo.getAllSubcategories())
        databaseJson["Expenses"] = Gson().toJson(repo.getAllExpenses())
        return databaseJson
    }
}