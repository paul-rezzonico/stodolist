package fr.unilim.stodolist.viewmodel

import fr.unilim.stodolist.models.Category
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.repository.CategoryRepository
import fr.unilim.stodolist.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Common ViewModel for Task and Category operations, shared across platforms.
 * 
 * This class does not extend AndroidX ViewModel to maintain cross-platform compatibility.
 * Platform-specific ViewModels should wrap this class and provide the appropriate CoroutineScope.
 *
 * @property taskRepository The task repository for data operations
 * @property categoryRepository The category repository for category operations
 * @property scope The coroutine scope for launching async operations
 */
class CommonTaskViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val scope: CoroutineScope
) {
    // ==========================================================================
    // Task State
    // ==========================================================================
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    /**
     * Flow of all tasks in the repository.
     * Emits updates whenever the underlying data changes.
     */
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    /**
     * Currently selected task, if any.
     */
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()
    
    // ==========================================================================
    // Category State
    // ==========================================================================
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    /**
     * Flow of all categories in the repository.
     * Emits updates whenever the underlying data changes.
     */
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _selectedFilterCategory = MutableStateFlow<Long?>(null)
    /**
     * Currently selected category ID for filtering tasks.
     * Null means "All" (no filter).
     */
    val selectedFilterCategory: StateFlow<Long?> = _selectedFilterCategory.asStateFlow()

    // ==========================================================================
    // Secondary Constructor for backward compatibility
    // ==========================================================================
    
    /**
     * Secondary constructor for backward compatibility with existing code
     * that only uses TaskRepository.
     */
    constructor(
        repository: TaskRepository,
        scope: CoroutineScope
    ) : this(
        taskRepository = repository,
        categoryRepository = object : CategoryRepository {
            override fun getAllCategories() = kotlinx.coroutines.flow.flowOf(emptyList<Category>())
            override suspend fun getCategoryById(id: Long): Category? = null
            override suspend fun getCategoriesForTask(taskId: Long): List<Category> = emptyList()
            override suspend fun insertCategory(category: Category): Long = 0L
            override suspend fun updateCategory(category: Category) {}
            override suspend fun deleteCategory(id: Long) {}
            override suspend fun addCategoryToTask(taskId: Long, categoryId: Long) {}
            override suspend fun removeCategoryFromTask(taskId: Long, categoryId: Long) {}
            override suspend fun setCategoriesForTask(taskId: Long, categoryIds: List<Long>) {}
            override suspend fun initializeDefaultCategories() {}
        },
        scope = scope
    )

    init {
        loadTasks()
        scope.launch {
            categoryRepository.initializeDefaultCategories()
            loadCategories()
        }
    }

    // ==========================================================================
    // Task Operations
    // ==========================================================================

    /**
     * Load all tasks from the repository.
     * Starts collecting from the repository Flow and updates the tasks StateFlow.
     */
    fun loadTasks() {
        scope.launch {
            taskRepository.getAllTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    /**
     * Add a new task to the repository.
     *
     * @param title The title of the task
     * @param description Optional description of the task
     * @param dueDate Optional due date as Unix timestamp (milliseconds)
     * @param categoryIds List of category IDs to associate with the task
     */
    fun addTask(title: String, description: String?, dueDate: Long?, categoryIds: List<Long> = emptyList()) {
        scope.launch {
            val task = Task(
                title = title,
                description = description,
                dueDate = dueDate,
                isCompleted = false
            )
            val taskId = taskRepository.insertTask(task)
            
            // Set categories for the new task
            if (categoryIds.isNotEmpty()) {
                categoryRepository.setCategoriesForTask(taskId, categoryIds)
            }
        }
    }

    /**
     * Update an existing task in the repository.
     *
     * @param task The task with updated values
     */
    fun updateTask(task: Task) {
        scope.launch {
            taskRepository.updateTask(task)
        }
    }
    
    /**
     * Update an existing task with new category assignments.
     *
     * @param task The task with updated values
     * @param categoryIds List of category IDs to associate with the task
     */
    fun updateTask(task: Task, categoryIds: List<Long>) {
        scope.launch {
            taskRepository.updateTask(task)
            categoryRepository.setCategoriesForTask(task.id, categoryIds)
        }
    }

    /**
     * Toggle the completion status of a task.
     *
     * @param task The task to toggle
     */
    fun toggleTaskCompletion(task: Task) {
        scope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskRepository.updateTask(updatedTask)
        }
    }

    /**
     * Delete a task from the repository.
     *
     * @param task The task to delete
     */
    fun deleteTask(task: Task) {
        scope.launch {
            taskRepository.deleteTask(task)
        }
    }

    /**
     * Select a task for viewing or editing.
     *
     * @param task The task to select, or null to clear selection
     */
    fun selectTask(task: Task?) {
        _selectedTask.value = task
    }

    /**
     * Get a task by its ID.
     *
     * @param id The task ID
     * @param onResult Callback with the task if found
     */
    fun getTask(id: Long, onResult: (Task?) -> Unit) {
        scope.launch {
            val task = taskRepository.getTask(id)
            onResult(task)
        }
    }
    
    // ==========================================================================
    // Category Operations
    // ==========================================================================
    
    /**
     * Load all categories from the repository.
     * Starts collecting from the repository Flow and updates the categories StateFlow.
     */
    fun loadCategories() {
        scope.launch {
            categoryRepository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }
    
    /**
     * Initialize default categories if they don't exist.
     * This is called automatically on ViewModel initialization.
     */
    fun initializeDefaultCategories() {
        scope.launch {
            categoryRepository.initializeDefaultCategories()
        }
    }
    
    /**
     * Add a new category to the repository.
     *
     * @param name The display name of the category
     * @param colorHex The hex color code (e.g., "#5C6BC0")
     * @param icon The emoji icon for the category
     */
    fun addCategory(name: String, colorHex: String, icon: String) {
        scope.launch {
            val category = Category(
                name = name,
                colorHex = colorHex,
                icon = icon,
                isDefault = false
            )
            categoryRepository.insertCategory(category)
        }
    }
    
    /**
     * Update an existing category.
     *
     * @param category The category with updated values
     */
    fun updateCategory(category: Category) {
        scope.launch {
            categoryRepository.updateCategory(category)
        }
    }
    
    /**
     * Delete a category by its ID.
     * Only non-default categories can be deleted.
     *
     * @param categoryId The ID of the category to delete
     */
    fun deleteCategory(categoryId: Long) {
        scope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }
    
    /**
     * Set the category filter for displaying tasks.
     *
     * @param categoryId The category ID to filter by, or null to show all tasks
     */
    fun setFilterCategory(categoryId: Long?) {
        _selectedFilterCategory.value = categoryId
    }
    
    /**
     * Clear the category filter to show all tasks.
     */
    fun clearFilterCategory() {
        _selectedFilterCategory.value = null
    }
    
    /**
     * Get a category by its ID.
     *
     * @param id The category ID
     * @param onResult Callback with the category if found
     */
    fun getCategory(id: Long, onResult: (Category?) -> Unit) {
        scope.launch {
            val category = categoryRepository.getCategoryById(id)
            onResult(category)
        }
    }
    
    /**
     * Get categories for a specific task.
     *
     * @param taskId The task ID
     * @param onResult Callback with the list of categories
     */
    fun getCategoriesForTask(taskId: Long, onResult: (List<Category>) -> Unit) {
        scope.launch {
            val categories = categoryRepository.getCategoriesForTask(taskId)
            onResult(categories)
        }
    }
}
