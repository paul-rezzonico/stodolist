package fr.unilim.stodolist.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.models.Category
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import fr.unilim.stodolist.database.Category as DbCategory

/**
 * SQLDelight implementation of CategoryRepository.
 * Handles all Category data operations using the generated SQLDelight queries.
 *
 * @property database The SQLDelight database instance
 * @property dispatcher The coroutine dispatcher for database operations (defaults to Dispatchers.Default)
 */
class CategoryRepositoryImpl(
    private val database: StodolistDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CategoryRepository {

    private val queries = database.categoryQueries

    /**
     * Get all categories as a Flow that automatically emits updates when data changes.
     * @return Flow emitting list of all categories mapped to domain model
     */
    override fun getAllCategories(): Flow<List<Category>> {
        return queries.getAllCategories()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbCategories -> dbCategories.map { it.toCategory() } }
    }

    /**
     * Get a single category by its ID.
     * @param id The unique identifier of the category
     * @return The category if found, null otherwise
     */
    override suspend fun getCategoryById(id: Long): Category? = withContext(dispatcher) {
        queries.getCategoryById(id = id).executeAsOneOrNull()?.toCategory()
    }

    /**
     * Get all categories associated with a specific task.
     * @param taskId The ID of the task
     * @return List of categories for the task
     */
    override suspend fun getCategoriesForTask(taskId: Long): List<Category> = withContext(dispatcher) {
        queries.getCategoriesForTask(taskId = taskId).executeAsList().map { it.toCategory() }
    }

    /**
     * Insert a new category into the database.
     * @param category The category to insert
     * @return The ID of the inserted category
     */
    override suspend fun insertCategory(category: Category): Long = withContext(dispatcher) {
        queries.insertCategory(
            name = category.name,
            colorHex = category.colorHex,
            icon = category.icon,
            isDefault = if (category.isDefault) 1L else 0L
        )
        queries.lastInsertCategoryId().executeAsOne()
    }

    /**
     * Update an existing category in the database.
     * @param category The category with updated values
     */
    override suspend fun updateCategory(category: Category) = withContext(dispatcher) {
        queries.updateCategory(
            name = category.name,
            colorHex = category.colorHex,
            icon = category.icon,
            id = category.id
        )
    }

    /**
     * Delete a category by its ID.
     * Only non-default categories can be deleted (enforced by SQL query).
     * @param id The ID of the category to delete
     */
    override suspend fun deleteCategory(id: Long) = withContext(dispatcher) {
        queries.deleteCategoryById(id = id)
    }

    /**
     * Add a category to a task (creates association).
     * Uses INSERT OR IGNORE to avoid duplicates.
     * @param taskId The ID of the task
     * @param categoryId The ID of the category to add
     */
    override suspend fun addCategoryToTask(taskId: Long, categoryId: Long) = withContext(dispatcher) {
        queries.addCategoryToTask(taskId = taskId, categoryId = categoryId)
    }

    /**
     * Remove a category from a task (removes association).
     * @param taskId The ID of the task
     * @param categoryId The ID of the category to remove
     */
    override suspend fun removeCategoryFromTask(taskId: Long, categoryId: Long) = withContext(dispatcher) {
        queries.removeCategoryFromTask(taskId = taskId, categoryId = categoryId)
    }

    /**
     * Set the categories for a task, replacing any existing associations.
     * This clears all existing category associations and creates new ones.
     * @param taskId The ID of the task
     * @param categoryIds List of category IDs to associate with the task
     */
    override suspend fun setCategoriesForTask(taskId: Long, categoryIds: List<Long>) = withContext(dispatcher) {
        database.transaction {
            queries.clearCategoriesFromTask(taskId = taskId)
            categoryIds.forEach { categoryId ->
                queries.setCategoriesForTask(taskId = taskId, categoryId = categoryId)
            }
        }
    }

    /**
     * Initialize default categories if they don't exist.
     * Checks if any default categories exist, and if not, inserts all predefined defaults.
     */
    override suspend fun initializeDefaultCategories() = withContext(dispatcher) {
        val defaultCount = queries.countDefaultCategories().executeAsOne()
        if (defaultCount == 0L) {
            database.transaction {
                Category.defaultCategories.forEach { category ->
                    queries.insertCategory(
                        name = category.name,
                        colorHex = category.colorHex,
                        icon = category.icon,
                        isDefault = 1L
                    )
                }
            }
        }
    }

    /**
     * Extension function to convert database Category entity to domain Category model.
     */
    private fun DbCategory.toCategory(): Category {
        return Category(
            id = this.id,
            name = this.name,
            colorHex = this.colorHex,
            icon = this.icon,
            isDefault = this.isDefault == 1L
        )
    }
}
