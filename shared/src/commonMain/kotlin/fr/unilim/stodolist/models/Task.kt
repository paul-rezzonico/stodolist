package fr.unilim.stodolist.models

import kotlinx.serialization.Serializable

/**
 * Data class representing a Task in the todo list.
 *
 * @property id Unique identifier for the task
 * @property title The title/name of the task
 * @property description Optional detailed description of the task
 * @property dueDate Optional due date as Unix timestamp (milliseconds)
 * @property isCompleted Whether the task has been completed
 * @property categories List of categories this task belongs to
 */
@Serializable
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val categories: List<Category> = emptyList()
)
