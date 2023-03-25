package fr.unilim.stodolist.repository

import androidx.lifecycle.LiveData
import fr.unilim.stodolist.dao.TaskDao
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }

    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}
