package fr.unilim.stodolist.repository

import fr.unilim.stodolist.models.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task data operations.
 * Implementations should handle data persistence using SQLDelight.
 */
interface TaskRepository {

    /**
     * Get all tasks as a Flow that emits updates when the data changes.
     * @return Flow emitting list of all tasks
     */
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Get pending (incomplete) tasks as a Flow.
     * @return Flow emitting list of pending tasks
     */
    fun getPendingTasks(): Flow<List<Task>>

    /**
     * Get completed tasks as a Flow.
     * @return Flow emitting list of completed tasks
     */
    fun getCompletedTasks(): Flow<List<Task>>

    /**
     * Get a single task by its ID.
     * @param id The unique identifier of the task
     * @return The task if found, null otherwise
     */
    suspend fun getTask(id: Long): Task?

    /**
     * Get overdue tasks (due date in the past and not completed).
     * @param currentTimeMillis The current time in milliseconds since epoch
     * @return List of overdue tasks
     */
    suspend fun getOverdueTasks(currentTimeMillis: Long): List<Task>

    /**
     * Count all tasks.
     * @return Total number of tasks
     */
    suspend fun countAllTasks(): Long

    /**
     * Count completed tasks.
     * @return Number of completed tasks
     */
    suspend fun countCompletedTasks(): Long

    /**
     * Insert a new task into the repository.
     * @param task The task to insert
     * @return The ID of the inserted task
     */
    suspend fun insertTask(task: Task): Long

    /**
     * Update an existing task.
     * @param task The task with updated values
     */
    suspend fun updateTask(task: Task)

    /**
     * Toggle task completion status.
     * @param taskId The ID of the task to toggle
     */
    suspend fun toggleTaskCompletion(taskId: Long)

    /**
     * Delete a task by its ID.
     * @param taskId The ID of the task to delete
     */
    suspend fun deleteTaskById(taskId: Long)

    /**
     * Delete a task from the repository.
     * @param task The task to delete
     */
    suspend fun deleteTask(task: Task)

    /**
     * Delete all completed tasks.
     */
    suspend fun deleteCompletedTasks()
}
