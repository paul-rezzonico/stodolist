package fr.unilim.stodolist.dao

import androidx.room.*
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE status = :status")
    suspend fun getTasksByStatus(status: TaskStatus): List<Task>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}