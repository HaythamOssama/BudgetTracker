package com.example.budgettracker

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.budgettracker.database.DatabaseRepo
import com.example.budgettracker.database.databasewrappers.TestingAppDatabase
import com.example.budgettracker.database.categories.Category
import com.example.budgettracker.database.expenses.Expense
import com.example.budgettracker.database.subcategory.Subcategory
import com.example.budgettracker.utils.VisibleForTestingOnly
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(VisibleForTestingOnly::class)
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var repo: DatabaseRepo
    private lateinit var database: TestingAppDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestingAppDatabase::class.java,
        ).allowMainThreadQueries().build()

        repo = DatabaseRepo(appContext, database)
    }

    @After
    fun teardown() {
        database.close()
    }

    /**
     * 1. Create a category object and insert it
     * 2. Get the category from DB and check that it's the same as inserted
     * 3. Delete the record
     * 4. Check that no records exist
     */
    @Test
    fun categoryInsertGetDelete() = runTest {
        val category = Category(name = "TestCategory")
        val categoryId = repo.insertCategory(category)
        assertEquals(1, categoryId)

        val retrievedCategory = repo.getCategoryByName("TestCategory")
        assertNotEquals(null, retrievedCategory, null)
        assertEquals(1, retrievedCategory.id)
        assertEquals("TestCategory", retrievedCategory.name)

        val deleteStatus = repo.deleteCategory(retrievedCategory)
        assertEquals(true, deleteStatus)

        assertEquals(0, database.categoryDao().getAll().size)
    }

    /**
     * 1. Create a category object and insert it and get its ID to create a new subcategory
     * 2. Get the subcategory from DB and check that it's the same as inserted
     * 3. Delete the record
     * 4. Check that no records exist
     */
    @Test
    fun subcategoryInsertGetDelete() = runTest {
        val category = Category(name = "TestCategory")
        val categoryId = repo.insertCategory(category)
        assertEquals(categoryId, 1)

        val subcategory = Subcategory(name = "TestSubcategory", categoryId = categoryId)
        val subcategoryId = repo.insertSubcategory(subcategory)
        assertEquals(subcategoryId, 1)

        val retrievedSubcategory = repo.getSubcategoryByName("TestSubcategory")
        assertNotEquals(null, retrievedSubcategory)
        assertEquals(1, retrievedSubcategory.id)
        assertEquals("TestSubcategory", retrievedSubcategory.name)
        assertEquals(1, retrievedSubcategory.categoryId,)
        assertEquals(1, database.subcategoryDao().getAll().size)

        val deleteStatus = repo.deleteSubcategory(retrievedSubcategory)
        assertEquals(true, deleteStatus)

        assertEquals(0, database.subcategoryDao().getAll().size)
    }

    /**
     * 1. Create a category and subcategory and insert them
     * 2. Check the live data object that a map is generated with them linked together
     */

    @Test
    fun testCategorySubcategoryMap() = runTest {
        val category = Category(name = "TestCategory")
        val categoryId = repo.insertCategory(category)

        val subcategory = Subcategory(name = "TestSubcategory", categoryId = categoryId)
        repo.insertSubcategory(subcategory)

        val categorySubcategoryMap = repo.allCategories.getOrAwaitValue()
        assertEquals(1, categorySubcategoryMap.size)

        val parsedCategoryList = repo.getParsedCategories(categorySubcategoryMap)

        val parsedCategory = parsedCategoryList[0]
        assertEquals("TestCategory", parsedCategory.name)
        assertEquals(1, parsedCategory.id)
        assertEquals(1, parsedCategory.subcategories.size)
        assertEquals("TestSubcategory", parsedCategory.subcategories[0].name)
        assertEquals(1, parsedCategory.subcategories[0].categoryId)
        assertEquals(parsedCategory, parsedCategory.subcategories[0].category)
    }

    /**
     * 1. Create a category, subcategory and an expense then insert them
     * 2. Check the expense live data and parse them
     * 3. Update the record to valid entries then retrieve the record and check the updated content
     * 4. Update the record with invalid subcategory and check that nothing changed
     * 5. Delete the record and check that nothing exists in the expenses table
     */
    @Test
    fun testExpenseMap() = runTest {
        val category = Category(name = "TestCategory")
        val categoryId = repo.insertCategory(category)
        category.id = categoryId.toInt()

        val subcategory = Subcategory(name = "TestSubcategory", categoryId = categoryId)
        subcategory.category = category
        val subcategoryId = repo.insertSubcategory(subcategory)
        subcategory.id = subcategoryId.toInt()

        val expense = Expense(subcategoryId = subcategoryId, cost = 200.0, count = 30.0,
            date = LocalDate.parse("01-Jan-2017", DateTimeFormatter.ofPattern("d-MMM-yyyy")))

        val expenseId = repo.insertExpense(expense)
        expense.id = expenseId.toInt()
        assertEquals(1, expenseId)
        expense.subcategory = repo.getParsedExpenses(database.expenseDao().getExpenseMappedById(expenseId.toInt()))[0].subcategory

        var rawExpenses = repo.allExpenses.getOrAwaitValue()
        var decodedExpenses = repo.getParsedExpenses(rawExpenses)
        assertEquals(1, decodedExpenses.size)

        var decodedExpense = decodedExpenses[0]
        assertEquals(200.0, decodedExpense.cost, 0.0)
        assertEquals(30.0, decodedExpense.count, 0.0)
        assertEquals(LocalDate.parse("01-Jan-2017", DateTimeFormatter.ofPattern("d-MMM-yyyy")), decodedExpense.date)
        assertNotEquals(null, decodedExpense.subcategory)
        assertEquals(subcategory, decodedExpense.subcategory)
        assertEquals(category, decodedExpense.subcategory?.category)
        assertEquals(mutableListOf(subcategory), decodedExpense.subcategory?.category!!.subcategories)

        /* Change the content of the expense and update the record */
        decodedExpense.date = LocalDate.parse("01-Jan-2023", DateTimeFormatter.ofPattern("d-MMM-yyyy"))
        decodedExpense.cost = 300.0
        decodedExpense.count = 40.0
        repo.updateExpense(decodedExpense)

        rawExpenses = repo.allExpenses.getOrAwaitValue()
        decodedExpenses = repo.getParsedExpenses(rawExpenses)
        assertEquals(1, decodedExpenses.size)

        decodedExpense = decodedExpenses[0]
        assertEquals(300.0, decodedExpense.cost, 0.0)
        assertEquals(40.0, decodedExpense.count, 0.0)
        assertEquals(LocalDate.parse("01-Jan-2023", DateTimeFormatter.ofPattern("d-MMM-yyyy")), decodedExpense.date)
        assertNotEquals(null, decodedExpense.subcategory)
        assertEquals(subcategory, decodedExpense.subcategory)
        assertEquals(category, decodedExpense.subcategory?.category)
        assertEquals(mutableListOf(subcategory), decodedExpense.subcategory?.category!!.subcategories)

        /* Change the content of the expense to wrong subcategory ID and update the record */
        decodedExpense.subcategoryId = 40
        assertEquals(false, repo.updateExpense(decodedExpense))

        /* No update should occur */
        rawExpenses = repo.allExpenses.getOrAwaitValue()
        decodedExpenses = repo.getParsedExpenses(rawExpenses)
        assertEquals(1, decodedExpenses.size)

        decodedExpense = decodedExpenses[0]
        assertEquals(300.0, decodedExpense.cost, 0.0)
        assertEquals(40.0, decodedExpense.count, 0.0)
        assertEquals(LocalDate.parse("01-Jan-2023", DateTimeFormatter.ofPattern("d-MMM-yyyy")), decodedExpense.date)
        assertNotEquals(null, decodedExpense.subcategory)
        assertEquals(subcategory, decodedExpense.subcategory)
        assertEquals(category, decodedExpense.subcategory?.category)
        assertEquals(mutableListOf(subcategory), decodedExpense.subcategory?.category!!.subcategories)

        val deleteStatus = repo.deleteExpense(expense)
        assertEquals(true, deleteStatus)

        val currentExpenseRecords = repo.allExpenses.getOrAwaitValue()
        assertEquals(0, currentExpenseRecords.size)
    }

    /**
     * 1. Create a category and subcategory then delete the category, the subcategory record should be deleted as well.
     * 2. Create a category and subcategory then delete the subcategory, the category record should not be deleted.
     */
    @Test
    fun testDeleteCategorySubcategory() = runTest {
        val snippet1 = run {
            val category = Category(name = "TestCategory")
            val categoryId = repo.insertCategory(category)
            category.id = categoryId.toInt()

            val subcategory = Subcategory(name = "TestSubcategory", categoryId = categoryId)
            subcategory.category = category
            val subcategoryId = repo.insertSubcategory(subcategory)
            subcategory.id = subcategoryId.toInt()

            repo.deleteCategory(category)
            assertEquals(0, database.subcategoryDao().getAll().size)
            assertEquals(0, database.categoryDao().getAll().size)
        }

        val snippet2 = run {
            val category = Category(name = "TestCategory")
            val categoryId = repo.insertCategory(category)
            category.id = categoryId.toInt()

            val subcategory = Subcategory(name = "TestSubcategory", categoryId = categoryId)
            subcategory.category = category
            val subcategoryId = repo.insertSubcategory(subcategory)
            subcategory.id = subcategoryId.toInt()

            repo.deleteSubcategory(subcategory)
            assertEquals(0, database.subcategoryDao().getAll().size)
            assertEquals(1, database.categoryDao().getAll().size)
        }

        snippet1.run {  }
        snippet2.run {  }
    }
}