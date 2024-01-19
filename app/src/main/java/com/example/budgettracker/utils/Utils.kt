package com.example.budgettracker.utils

import android.app.Activity
import android.os.Build
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        activity.intent.getSerializableExtra(name, clazz)!!
    else
        activity.intent.getSerializableExtra(name) as T
}

fun getGlobalSimpleDateFormat(): SimpleDateFormat {
    return SimpleDateFormat("dd MMMM yyyy", Locale.UK)
}