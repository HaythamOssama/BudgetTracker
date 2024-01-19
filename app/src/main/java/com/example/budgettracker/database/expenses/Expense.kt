package com.example.budgettracker.database.expenses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.budgettracker.database.subcategory.Subcategory
import java.io.Serializable

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
    var cost: Double,
    var count: Double,
    var date: String,
) : Serializable {
    @Ignore
    var subcategory: Subcategory?= null

    override fun toString(): String {
        return String.format("Expense: [%s] [count: %s] [cost: %s] [date: %s] [%s]",
            id, count, cost, date, subcategory.toString())
    }

    fun isStringPresent(item: String): Boolean {
        return (cost.toString().contains(item) ||
                count.toString().contains(item) ||
                date.contains(item) ||
                subcategory!!.name.contains(item)||
                subcategory!!.category!!.name.contains(item))
    }
}