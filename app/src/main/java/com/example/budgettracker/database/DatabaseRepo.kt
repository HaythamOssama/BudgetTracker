package com.example.budgettracker.database

import android.content.Context
import androidx.annotation.WorkerThread
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.databasewrappers.AppDatabase
import com.example.budgettracker.database.databasewrappers.TestingAppDatabase
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.expenses.PayType
import com.example.budgettracker.database.subcategory.Subcategory

class DatabaseRepo(context: Context, initializedDatabase: TestingAppDatabase? = null) {
    private var database = initializedDatabase?: AppDatabase.getInstance(context)

    // Entity DAOs
    private var categoryDao = database.categoryDao()
    private var subcategoryDao = database.subcategoryDao()
    private var expenseDao = database.expenseDao()

    // Live Objects
    val allCategories = categoryDao.getAllLive()
    val allExpenses = expenseDao.getAllLive()

    /*********** Category Entity Functions ***********/
    @WorkerThread
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category)
    }

    @WorkerThread
    suspend fun getCategoryByName(categoryName: String): Category? {
        val category = categoryDao.get(categoryName)
        if (category != null) {
            category.subcategories = subcategoryDao.get(category.id.toLong())
        }
        return category
    }

    fun getParsedCategories(rawCategorySubcategoryMap: Map<Category, List<Subcategory>>) : List<Category> {
        val parsedCategories = mutableListOf<Category>()
        rawCategorySubcategoryMap.forEach { entry ->
            val category = entry.key
            category.subcategories = entry.value
            category.subcategories.forEach {
                it.category = category
            }
            parsedCategories.add(category)
        }
        return parsedCategories
    }

    @WorkerThread
    suspend fun deleteCategory(category: Category): Boolean{
        return categoryDao.delete(category) > 0
    }

    /*********** Subcategory Entity Functions ***********/
    @WorkerThread
    suspend fun insertSubcategory(subcategory: Subcategory): Long {
        return subcategoryDao.insert(subcategory)
    }

    @WorkerThread
    suspend fun getSubcategoryByName(name: String): Subcategory? {
        val subcategory = subcategoryDao.get(name)
        if (subcategory != null) {
            subcategory.category = categoryDao.get(subcategory.categoryId)
        }
        return subcategory
    }

    @WorkerThread
    suspend fun updateSubcategory(subcategory: Subcategory) : Boolean {
        return subcategoryDao.update(subcategory) > 0
    }

    @WorkerThread
    suspend fun deleteSubcategory(subcategory: Subcategory): Boolean{
        return subcategoryDao.delete(subcategory) > 0
    }

    /*********** Expenses Entity Functions ***********/
    @WorkerThread
    suspend fun insertExpense(expense: Expense): Boolean {
        return expenseDao.insert(expense) > 0
    }

    @WorkerThread
    suspend fun getParsedExpenses(rawExpensesMap : Map<Expense, Map<Subcategory, Category>>): List<Expense> {
        val expensesList: MutableList<Expense> = mutableListOf()

        for (item in rawExpensesMap) {
            /**
             * 1 to 1 to 1 Relation between Expense -> Subcategory -> Category
             * Key: Expense Object
             * Value: [Key -> Subcategory , Value -> Category]
             */
            val expense: Expense = item.key

            assert(item.value.keys.size == 1)
            assert(item.value.values.size == 1)

            expense.subcategory = item.value.keys.elementAt(0)
            expense.subcategory!!.category = item.value.values.elementAt(0)
            expense.subcategory!!.category!!.subcategories = subcategoryDao.get(expense.subcategory!!.category!!.id.toLong())
            for (subcategory in expense.subcategory!!.category!!.subcategories) {
                subcategory.category = expense.subcategory!!.category
            }

            expensesList.add(expense)
        }

        return expensesList.toList()
    }

    @WorkerThread
    suspend fun updateExpense(expense: Expense, inputCategory: String, inputSubcategory: String,
                              inputCost: String, inputCount: String, inputDate: String,
                              payType: PayType): Boolean
    {
        val isCategorySame = expense.subcategory!!.category!!.name == inputCategory
        val isSubcategorySame = expense.subcategory!!.name == inputSubcategory
        var categoryId: Long = expense.subcategory!!.category!!.id.toLong()
        var subcategoryId: Long = expense.subcategoryId

        expense.cost = inputCost.toDouble()
        expense.count = inputCount.toDouble()
        expense.date = inputDate
        expense.payType = payType

        if(!isCategorySame) {
            val category = getCategoryByName(inputCategory)
            categoryId = category?.id?.toLong() ?: insertCategory(Category(name = inputCategory))

            if(isSubcategorySame) {
                expense.subcategory!!.categoryId = categoryId
                updateSubcategory(expense.subcategory!!)
            }
        }

        if(!isSubcategorySame) {
            val subcategory = getSubcategoryByName(inputSubcategory)
            subcategoryId = subcategory?.id?.toLong() ?: insertSubcategory(Subcategory(name = inputSubcategory, categoryId = categoryId))
        }

        expense.subcategoryId = subcategoryId

        return expenseDao.update(expense) > 0
    }

    @WorkerThread
    suspend fun deleteExpense(expense: Expense): Boolean{
        return expenseDao.delete(expense) > 0
    }
}