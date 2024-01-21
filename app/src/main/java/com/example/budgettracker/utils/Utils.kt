package com.example.budgettracker.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt


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

fun dpToPx(dp: Int, context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}
