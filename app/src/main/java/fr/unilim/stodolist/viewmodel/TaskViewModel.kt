package fr.unilim.stodolist.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.notifications.TaskNotificationReceiver
import fr.unilim.stodolist.repository.TaskRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

    fun scheduleTaskNotification(context: Context, task: Task) {
        val notificationId = task.id
        val alarmIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
            putExtra("notificationId", notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Remplacer cette valeur par le nombre de millisecondes avant la date d'échéance pour envoyer la notification
        val notificationTimeBeforeDueDate = TimeUnit.HOURS.toMillis(1)

        task.dueDate?.let { dueDate ->
            val triggerAtMillis = dueDate.time - notificationTimeBeforeDueDate
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

}
