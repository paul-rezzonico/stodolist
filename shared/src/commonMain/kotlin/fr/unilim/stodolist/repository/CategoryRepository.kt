package fr.unilim.stodolist.repository

import fr.unilim.stodolist.models.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Category data operations.
 * Implementations should handle data persistence using SQLDelight.
 */
interface CategoryRepository {

    /**
     * Get all categories as a Flow that emits updates when the data changes.
     * Categories are ordered by isDefault (descending) and name (ascending).
     * @return Flow emitting list of all categories
     */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Get a single category by its ID.
     * @param id The unique identifier of the category
     * @return The category if found, null otherwise
     */
    suspend fun getCategoryById(id: Long): Category?

    /**
     * Get all categories associated with a specific task.
     * @param taskId The ID of the task
     * @return List of categories for the task
     */
    suspend fun getCategoriesForTask(taskId: Long): List<Category>

    /**
     * Insert a new category into the repository.
     * @param category The category to insert
     * @return The ID of the inserted category
     */
    suspend fun insertCategory(category: Category): Long

    /**
     * Update an existing category.
     * @param category The category with updated values
     */
    suspend fun updateCategory(category: Category)

    /**
     * Delete a category by its ID.
     * Only non-default categories can be deleted.
     * @param id The ID of the category to delete
     */
    suspend fun deleteCategory(id: Long)

    /**
     * Add a category to a task (creates association).
     * @param taskId The ID of the task
     * @param categoryId The ID of the category to add
     */
    suspend fun addCategoryToTask(taskId: Long, categoryId: Long)

    /**
     * Remove a category from a task (removes association).
     * @param taskId The ID of the task
     * @param categoryId The ID of the category to remove
     */
    suspend fun removeCategoryFromTask(taskId: Long, categoryId: Long)

    /**
     * Set the categories for a task, replacing any existing associations.
     * @param taskId The ID of the task
     * @param categoryIds List of category IDs to associate with the task
     */
    suspend fun setCategoriesForTask(taskId: Long, categoryIds: List<Long>)

    /**
     * Initialize default categories if they don't exist.
     * This should be called on app startup to ensure default categories are available.
     */
    suspend fun initializeDefaultCategories()
}
