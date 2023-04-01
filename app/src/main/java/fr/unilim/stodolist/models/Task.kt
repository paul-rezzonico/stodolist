package fr.unilim.stodolist.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String? = null,

    val dueDate: Date? = null,

    val status: TaskStatus = TaskStatus.TODO
)

enum class TaskStatus {
    TODO,
    LATE,
    COMPLETED,
}
