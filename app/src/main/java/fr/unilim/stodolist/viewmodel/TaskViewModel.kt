package fr.unilim.stodolist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    fun getAllTasks(): LiveData<List<Task>> = taskRepository.getAllTasks()

    fun getTasksByStatus(status: TaskStatus): LiveData<List<Task>> =
        taskRepository.getTasksByStatus(status)

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
    }
}
