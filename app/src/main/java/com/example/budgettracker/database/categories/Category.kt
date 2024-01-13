package com.example.budgettracker.database.categories

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.budgettracker.database.subcategory.Subcategory
import java.io.Serializable

@Entity(tableName = "Categories", indices = [
    Index(value = ["name"], unique = true) ])
data class Category (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String
) : Serializable {
    @Ignore
    var subcategories: List<Subcategory> = mutableListOf()

    override fun toString(): String {
        var literal = String.format("Category [%s] [%s]\n", id, name)
        for(subcategory in subcategories) {
            literal += subcategory.toString() + "\n"
        }
        literal += "\n"
        return literal
    }
}