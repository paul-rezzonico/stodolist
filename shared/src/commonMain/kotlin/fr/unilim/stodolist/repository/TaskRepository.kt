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
     * Get a single task by its ID.
     * @param id The unique identifier of the task
     * @return The task if found, null otherwise
     */
    suspend fun getTask(id: Long): Task?

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
     * Delete a task from the repository.
     * @param task The task to delete
     */
    suspend fun deleteTask(task: Task)
}
