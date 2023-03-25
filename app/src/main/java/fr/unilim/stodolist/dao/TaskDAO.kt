package fr.unilim.stodolist.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}