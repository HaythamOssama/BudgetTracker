package com.example.budgettracker.database.expenses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.budgettracker.database.subcategory.Subcategory
import java.time.LocalDate

@Entity(tableName = "Expenses",
    foreignKeys = [
        ForeignKey(entity = Subcategory::class,
        parentColumns = ["id"],
        childColumns = ["subcategoryId"]
    )]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var subcategoryId: Long,
    var date: LocalDate,
    var count: Double,
    var cost: Double,
) {
    @Ignore
    var subcategory: Subcategory?= null

    override fun toString(): String {
        return String.format("Expense: [%s] [count: %s] [cost: %s] [date: %s] [%s]",
            id, count, cost, date.toString(), subcategory.toString())
    }
}