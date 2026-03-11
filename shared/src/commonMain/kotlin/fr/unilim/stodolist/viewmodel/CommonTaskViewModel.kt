package fr.unilim.stodolist.viewmodel

import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Common ViewModel for Task operations, shared across platforms.
 * 
 * This class does not extend AndroidX ViewModel to maintain cross-platform compatibility.
 * Platform-specific ViewModels should wrap this class and provide the appropriate CoroutineScope.
 *
 * @property repository The task repository for data operations
 * @property scope The coroutine scope for launching async operations
 */
class CommonTaskViewModel(
    private val repository: TaskRepository,
    private val scope: CoroutineScope
) {
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

    init {
        loadTasks()
    }

    /**
     * Load all tasks from the repository.
     * Starts collecting from the repository Flow and updates the tasks StateFlow.
     */
    fun loadTasks() {
        scope.launch {
            repository.getAllTasks().collect { taskList ->
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
     */
    fun addTask(title: String, description: String?, dueDate: Long?) {
        scope.launch {
            val task = Task(
                title = title,
                description = description,
                dueDate = dueDate,
                isCompleted = false
            )
            repository.insertTask(task)
        }
    }

    /**
     * Update an existing task in the repository.
     *
     * @param task The task with updated values
     */
    fun updateTask(task: Task) {
        scope.launch {
            repository.updateTask(task)
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
            repository.updateTask(updatedTask)
        }
    }

    /**
     * Delete a task from the repository.
     *
     * @param task The task to delete
     */
    fun deleteTask(task: Task) {
        scope.launch {
            repository.deleteTask(task)
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
            val task = repository.getTask(id)
            onResult(task)
        }
    }
}
