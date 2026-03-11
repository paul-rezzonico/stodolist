package fr.unilim.stodolist.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import fr.unilim.stodolist.database.Task as DbTask

/**
 * SQLDelight implementation of TaskRepository.
 * Handles all Task data operations using the generated SQLDelight queries.
 *
 * @property database The SQLDelight database instance
 */
class TaskRepositoryImpl(
    private val database: StodolistDatabase
) : TaskRepository {

    private val queries = database.taskQueries

    /**
     * Get all tasks as a Flow that automatically emits updates when data changes.
     * @return Flow emitting list of all tasks mapped to domain model
     */
    override fun getAllTasks(): Flow<List<Task>> {
        return queries.getAllTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbTasks -> dbTasks.map { it.toTask() } }
    }

    /**
     * Get a single task by its ID.
     * @param id The unique identifier of the task
     * @return The task if found, null otherwise
     */
    override suspend fun getTask(id: Long): Task? = withContext(Dispatchers.Default) {
        queries.getTaskById(id).executeAsOneOrNull()?.toTask()
    }

    /**
     * Insert a new task into the database.
     * @param task The task to insert
     * @return The ID of the inserted task (always 0 for SQLDelight INSERT without RETURNING)
     */
    override suspend fun insertTask(task: Task): Long = withContext(Dispatchers.Default) {
        queries.insertTask(
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            isCompleted = if (task.isCompleted) 1L else 0L
        )
        // SQLDelight doesn't return the inserted ID directly, return 0
        // In a real app, you might use a custom query with RETURNING
        0L
    }

    /**
     * Update an existing task in the database.
     * @param task The task with updated values
     */
    override suspend fun updateTask(task: Task) = withContext(Dispatchers.Default) {
        queries.updateTask(
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            isCompleted = if (task.isCompleted) 1L else 0L,
            id = task.id
        )
    }

    /**
     * Delete a task from the database.
     * @param task The task to delete
     */
    override suspend fun deleteTask(task: Task) = withContext(Dispatchers.Default) {
        queries.deleteTask(task.id)
    }

    /**
     * Extension function to convert database Task entity to domain Task model.
     */
    private fun DbTask.toTask(): Task {
        return Task(
            id = this.id,
            title = this.title,
            description = this.description,
            dueDate = this.dueDate,
            isCompleted = this.isCompleted == 1L
        )
    }
}
