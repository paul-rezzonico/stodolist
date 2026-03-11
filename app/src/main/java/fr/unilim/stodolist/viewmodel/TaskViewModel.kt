@file:Suppress("DEPRECATION")

package fr.unilim.stodolist.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.notifications.TaskNotificationReceiver
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Android ViewModel that wraps the CommonTaskViewModel from the shared module.
 * 
 * Provides backward compatibility with existing Fragment code while delegating
 * to the cross-platform CommonTaskViewModel for business logic.
 *
 * @property commonViewModel The shared CommonTaskViewModel instance
 * 
 * @deprecated This ViewModel wrapper is deprecated. The app now uses CommonTaskViewModel 
 * directly from the shared module. See [fr.unilim.stodolist.ui.MainActivity] for the new approach.
 * This file is kept for backward compatibility and notification scheduling.
 */
@Deprecated(
    message = "Use CommonTaskViewModel from shared module directly. See MainActivity.",
    level = DeprecationLevel.WARNING
)
class TaskViewModel(
    private val commonViewModel: CommonTaskViewModel
) : ViewModel() {

    /**
     * Flow of all tasks.
     */
    val tasksFlow: StateFlow<List<Task>> = commonViewModel.tasks

    /**
     * Flow of the currently selected task.
     */
    val selectedTaskFlow: StateFlow<Task?> = commonViewModel.selectedTask

    /**
     * LiveData of all tasks for backward compatibility with existing UI code.
     * Maps shared Task model to include computed status based on due date.
     */
    fun getAllTasks() = tasksFlow.map { tasks ->
        tasks.map { task -> task.toAndroidTask() }
    }.asLiveData()

    /**
     * LiveData of tasks filtered by status for backward compatibility.
     */
    fun getTasksByStatus(status: TaskStatus) = tasksFlow.map { tasks ->
        tasks.map { it.toAndroidTask() }
            .filter { it.status == status }
    }.asLiveData()

    /**
     * Insert a new task.
     */
    fun insertTask(task: AndroidTask) = viewModelScope.launch {
        commonViewModel.addTask(
            title = task.title,
            description = task.description,
            dueDate = task.dueDate?.time
        )
    }

    /**
     * Update an existing task.
     */
    fun updateTask(task: AndroidTask) = viewModelScope.launch {
        val sharedTask = task.toSharedTask()
        commonViewModel.updateTask(sharedTask)
    }

    /**
     * Delete a task.
     */
    fun deleteTask(task: AndroidTask) = viewModelScope.launch {
        val sharedTask = task.toSharedTask()
        commonViewModel.deleteTask(sharedTask)
    }

    /**
     * Schedule a notification for a task.
     */
    fun scheduleTaskNotification(context: Context, task: AndroidTask) {
        val notificationId = task.id
        val alarmIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
            putExtra("notificationId", notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Notification time: 1 hour before due date
        val notificationTimeBeforeDueDate = TimeUnit.HOURS.toMillis(1)

        task.dueDate?.let { dueDate ->
            val triggerAtMillis = dueDate.time - notificationTimeBeforeDueDate
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}

/**
 * Android-specific Task class that includes status computation.
 * Used for UI layer compatibility with existing code.
 */
data class AndroidTask(
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDate: java.util.Date? = null,
    val status: TaskStatus = TaskStatus.TODO
)

/**
 * Task status enum for Android UI.
 */
enum class TaskStatus {
    TODO,
    LATE,
    COMPLETED
}

/**
 * Convert shared Task to Android Task with computed status.
 */
fun Task.toAndroidTask(): AndroidTask {
    val now = System.currentTimeMillis()
    val taskDueDate = dueDate
    val computedStatus = when {
        isCompleted -> TaskStatus.COMPLETED
        taskDueDate != null && taskDueDate < now -> TaskStatus.LATE
        else -> TaskStatus.TODO
    }
    return AndroidTask(
        id = id.toInt(),
        title = title,
        description = description,
        dueDate = taskDueDate?.let { java.util.Date(it) },
        status = computedStatus
    )
}

/**
 * Convert Android Task to shared Task.
 */
fun AndroidTask.toSharedTask(): Task {
    return Task(
        id = id.toLong(),
        title = title,
        description = description,
        dueDate = dueDate?.time,
        isCompleted = status == TaskStatus.COMPLETED
    )
}
