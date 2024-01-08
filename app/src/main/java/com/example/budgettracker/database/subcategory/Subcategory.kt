package com.example.budgettracker.database.subcategory

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.budgettracker.database.categories.Category

@Entity(tableName = "Subcategories",
    indices = [
        Index(value = ["name"], unique = true) ],
    foreignKeys = [
        ForeignKey(entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE)
    ])
data class Subcategory(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var categoryId: Long,
    var name: String,
) {
    @Ignore
    var category: Category?= null

    override fun toString(): String {
        return String.format("Subcategory [%s] [%s] -> [%s]\n", id, name, category.toString())
    }

}