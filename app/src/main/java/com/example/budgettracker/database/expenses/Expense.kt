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
    var payType: PayType
) : Serializable {
    @Ignore
    var subcategory: Subcategory?= null

    override fun toString(): String {
        return String.format("Expense: [%s] [count: %s] [cost: %s] [date: %s] [%s] [%s]",
            id, count, cost, date, payType.name, subcategory.toString())
    }

    fun isStringPresent(item: String): Boolean {
        return (cost.toString().contains(item, true) ||
                count.toString().contains(item, true) ||
                date.contains(item, true) ||
                subcategory!!.name.contains(item, true)||
                subcategory!!.category!!.name.contains(item, true))
    }
}

enum class PayType {
    PAY_TYPE_CASH,
    PAY_TYPE_DEBIT,
    PAY_TYPE_CREDIT;

    companion object {
        fun parse(literal: String): PayType {
            return when (literal) {
                "Cash" -> PAY_TYPE_CASH
                "Debit" -> PAY_TYPE_DEBIT
                "Credit" -> PAY_TYPE_CREDIT
                else -> PAY_TYPE_CASH
            }
        }

        fun parse(payType: PayType): String {
            return when(payType) {
                PAY_TYPE_CASH -> "Cash"
                PAY_TYPE_DEBIT -> "Debit"
                PAY_TYPE_CREDIT -> "Credit"
            }
        }
    }
}