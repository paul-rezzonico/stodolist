package fr.unilim.stodolist.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.models.Task
import kotlinx.coroutines.CoroutineDispatcher
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
 * @property dispatcher The coroutine dispatcher for database operations (defaults to Dispatchers.Default)
 */
class TaskRepositoryImpl(
    private val database: StodolistDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskRepository {

    private val queries = database.taskQueries

    /**
     * Get all tasks as a Flow that automatically emits updates when data changes.
     * @return Flow emitting list of all tasks mapped to domain model
     */
    override fun getAllTasks(): Flow<List<Task>> {
        return queries.getAllTasks()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbTasks -> dbTasks.map { it.toTask() } }
    }

    /**
     * Get pending (incomplete) tasks as a Flow.
     * @return Flow emitting list of pending tasks
     */
    override fun getPendingTasks(): Flow<List<Task>> {
        return queries.getPendingTasks()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbTasks -> dbTasks.map { it.toTask() } }
    }

    /**
     * Get completed tasks as a Flow.
     * @return Flow emitting list of completed tasks
     */
    override fun getCompletedTasks(): Flow<List<Task>> {
        return queries.getCompletedTasks()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbTasks -> dbTasks.map { it.toTask() } }
    }

    /**
     * Get a single task by its ID.
     * @param id The unique identifier of the task
     * @return The task if found, null otherwise
     */
    override suspend fun getTask(id: Long): Task? = withContext(dispatcher) {
        queries.getTaskById(id = id).executeAsOneOrNull()?.toTask()
    }

    /**
     * Get overdue tasks (due date in the past and not completed).
     * @param currentTimeMillis The current time in milliseconds since epoch
     * @return List of overdue tasks
     */
    override suspend fun getOverdueTasks(currentTimeMillis: Long): List<Task> = withContext(dispatcher) {
        queries.getOverdueTasks(currentTime = currentTimeMillis) { id, title, description, dueDate, isCompleted ->
            Task(
                id = id,
                title = title,
                description = description,
                dueDate = dueDate,
                isCompleted = isCompleted == 1L
            )
        }.executeAsList()
    }

    /**
     * Count all tasks.
     * @return Total number of tasks
     */
    override suspend fun countAllTasks(): Long = withContext(dispatcher) {
        queries.countAllTasks().executeAsOne()
    }

    /**
     * Count completed tasks.
     * @return Number of completed tasks
     */
    override suspend fun countCompletedTasks(): Long = withContext(dispatcher) {
        queries.countCompletedTasks().executeAsOne()
    }

    /**
     * Insert a new task into the database.
     * @param task The task to insert
     * @return The ID of the inserted task
     */
    override suspend fun insertTask(task: Task): Long = withContext(dispatcher) {
        queries.insertTask(
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            isCompleted = if (task.isCompleted) 1L else 0L
        )
        queries.lastInsertRowId().executeAsOne()
    }

    /**
     * Update an existing task in the database.
     * @param task The task with updated values
     */
    override suspend fun updateTask(task: Task) = withContext(dispatcher) {
        queries.updateTask(
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            isCompleted = if (task.isCompleted) 1L else 0L,
            id = task.id
        )
    }

    /**
     * Toggle task completion status.
     * @param taskId The ID of the task to toggle
     */
    override suspend fun toggleTaskCompletion(taskId: Long) = withContext(dispatcher) {
        queries.toggleTaskCompletion(id = taskId)
    }

    /**
     * Delete a task by its ID.
     * @param taskId The ID of the task to delete
     */
    override suspend fun deleteTaskById(taskId: Long) = withContext(dispatcher) {
        queries.deleteTaskById(id = taskId)
    }

    /**
     * Delete a task from the database.
     * @param task The task to delete
     */
    override suspend fun deleteTask(task: Task) = withContext(dispatcher) {
        queries.deleteTaskById(id = task.id)
    }

    /**
     * Delete all completed tasks.
     */
    override suspend fun deleteCompletedTasks() = withContext(dispatcher) {
        queries.deleteCompletedTasks()
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
