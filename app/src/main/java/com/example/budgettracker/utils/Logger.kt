package com.example.budgettracker.utils

import android.util.Log

class Logger {
    companion object {
        private const val CALLER_STACK_TRACE_INDEX = 4
        private const val LOG_TAG = "AppLog"

        fun logError(message: String?) {
            val errorMessage = String.format("%s\t%s", getLogSignature(), message)
            Log.e(LOG_TAG, errorMessage)
        }

        fun logDebug(message: String?) {
            val errorMessage = String.format("%s\t%s", getLogSignature(), message)
            Log.d(LOG_TAG, errorMessage)
        }

        fun logWarn(message: String?) {
            val errorMessage = String.format("%s\t%s", getLogSignature(), message)
            Log.w(LOG_TAG, errorMessage)
        }

        private fun getLogSignature(): String {
            val element =
                    Thread.currentThread().stackTrace[CALLER_STACK_TRACE_INDEX]
            val fullClassName =
                    element.className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val className = fullClassName[fullClassName.size - 1]
            val methodName = element.methodName
            val lineNumber = element.lineNumber
            return String.format("[%s]\t[%s]\t[%s]", className, methodName, lineNumber)
        }
    }
}