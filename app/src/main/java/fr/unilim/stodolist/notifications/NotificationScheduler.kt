package fr.unilim.stodolist.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import fr.unilim.stodolist.models.Task

/**
 * Helper class for scheduling and managing task reminder notifications.
 * 
 * Uses AlarmManager for exact alarm scheduling with proper handling of
 * Android 12+ alarm restrictions.
 */
object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    /**
     * Schedule a reminder notification for a task at the specified time.
     * 
     * @param context The application context
     * @param task The task to schedule a reminder for
     * @param reminderTimeMillis The time in milliseconds when the reminder should fire
     * @return true if the alarm was scheduled successfully, false otherwise
     */
    fun scheduleTaskReminder(
        context: Context,
        task: Task,
        reminderTimeMillis: Long
    ): Boolean {
        return scheduleTaskReminder(
            context = context,
            taskId = task.id,
            taskTitle = task.title,
            reminderTimeMillis = reminderTimeMillis
        )
    }

    /**
     * Schedule a reminder notification for a task at the specified time.
     * 
     * @param context The application context
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param reminderTimeMillis The time in milliseconds when the reminder should fire
     * @return true if the alarm was scheduled successfully, false otherwise
     */
    fun scheduleTaskReminder(
        context: Context,
        taskId: Long,
        taskTitle: String,
        reminderTimeMillis: Long
    ): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if we can schedule exact alarms (Android 12+)
        if (!canScheduleExactAlarms(context, alarmManager)) {
            Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
            return false
        }

        // Don't schedule alarms in the past
        if (reminderTimeMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Cannot schedule alarm in the past")
            return false
        }

        val pendingIntent = createPendingIntent(context, taskId, taskTitle)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Use setExactAndAllowWhileIdle for better reliability during Doze mode
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for task $taskId at $reminderTimeMillis")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            return false
        }
    }

    /**
     * Cancel a previously scheduled reminder for a task.
     * 
     * @param context The application context
     * @param taskId The ID of the task whose reminder should be cancelled
     */
    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context, taskId, "", forCancel = true)
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        
        Log.d(TAG, "Cancelled reminder for task $taskId")
    }

    /**
     * Check if the app can schedule exact alarms.
     * 
     * On Android 12 (API 31) and above, the SCHEDULE_EXACT_ALARM permission
     * must be granted by the user via system settings.
     * 
     * @param context The application context
     * @param alarmManager The AlarmManager instance
     * @return true if exact alarms can be scheduled, false otherwise
     */
    fun canScheduleExactAlarms(context: Context, alarmManager: AlarmManager? = null): Boolean {
        val manager = alarmManager ?: context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            // Before Android 12, exact alarms are always allowed
            true
        }
    }

    /**
     * Get the intent to open exact alarm settings (Android 12+).
     * 
     * @param context The application context
     * @return Intent to open the exact alarm settings, or null if not applicable
     */
    fun getExactAlarmSettingsIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        } else {
            null
        }
    }

    /**
     * Check if a reminder is scheduled for a specific task.
     * 
     * @param context The application context
     * @param taskId The ID of the task to check
     * @return true if a reminder is scheduled, false otherwise
     */
    fun isReminderScheduled(context: Context, taskId: Long): Boolean {
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, taskId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        return pendingIntent != null
    }

    /**
     * Schedule a reminder based on task's due date.
     * By default, schedules 30 minutes before the due date.
     * 
     * @param context The application context
     * @param task The task to schedule a reminder for
     * @param minutesBefore How many minutes before the due date to remind (default: 30)
     * @return true if scheduled successfully, false otherwise
     */
    fun scheduleReminderBeforeDueDate(
        context: Context,
        task: Task,
        minutesBefore: Int = 30
    ): Boolean {
        val dueDate = task.dueDate ?: return false
        val reminderTime = dueDate - (minutesBefore * 60 * 1000L)
        
        return scheduleTaskReminder(context, task, reminderTime)
    }

    /**
     * Create a PendingIntent for the task reminder.
     * Uses a unique request code based on taskId to allow multiple alarms.
     */
    private fun createPendingIntent(
        context: Context,
        taskId: Long,
        taskTitle: String,
        forCancel: Boolean = false
    ): PendingIntent {
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskNotificationReceiver.EXTRA_NOTIFICATION_ID, taskId.toInt())
        }

        val flags = if (forCancel) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(), // Unique request code per task
            intent,
            flags
        )
    }
}
