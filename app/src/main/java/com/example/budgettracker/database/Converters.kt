package com.example.budgettracker.database

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromDate(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDate(date: Long?): LocalDate? {
        return LocalDate.ofEpochDay(date!!)
    }

}