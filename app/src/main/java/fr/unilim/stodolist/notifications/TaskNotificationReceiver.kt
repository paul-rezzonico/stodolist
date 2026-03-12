package fr.unilim.stodolist.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import fr.unilim.stodolist.R
import fr.unilim.stodolist.ui.MainActivity

/**
 * BroadcastReceiver that handles task notification delivery.
 * 
 * Features:
 * - Permission check before posting notifications (Android 13+)
 * - Notification channel creation (Android 8+)
 * - Tap action to open the app
 * - Action buttons for Mark Complete and Snooze
 * - Vibration pattern for attention
 */
class TaskNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "task_notifications"
        const val CHANNEL_NAME = "Rappels de tâches"
        const val CHANNEL_DESCRIPTION = "Notifications pour les rappels de tâches"

        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_TASK_TITLE = "taskTitle"
        const val EXTRA_NOTIFICATION_ID = "notificationId"

        const val ACTION_MARK_COMPLETE = "fr.unilim.stodolist.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "fr.unilim.stodolist.ACTION_SNOOZE"

        // Vibration pattern: wait 0ms, vibrate 250ms, pause 250ms, vibrate 250ms
        private val VIBRATION_PATTERN = longArrayOf(0, 250, 250, 250)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, taskId.toInt())

        // Handle action buttons
        when (intent.action) {
            ACTION_MARK_COMPLETE -> {
                handleMarkComplete(context, taskId, notificationId)
                return
            }
            ACTION_SNOOZE -> {
                handleSnooze(context, taskId, taskTitle, notificationId)
                return
            }
        }

        // Check notification permission on Android 13+
        if (!isNotificationPermissionGranted(context)) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (Android 8+)
        createNotificationChannel(notificationManager)

        // Build and show the notification
        val notification = buildNotification(context, taskId, taskTitle, notificationId)
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Check if notification permission is granted.
     * On Android 12 and below, this always returns true.
     */
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Create the notification channel if it doesn't exist.
     * Only creates channel once (system ignores duplicate creations).
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel already exists
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null) {
                return
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Build the notification with all features.
     */
    private fun buildNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        notificationId: Int
    ): android.app.Notification {
        // Create intent to open the app when tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Mark Complete action
        val markCompleteIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1, // Unique request code
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Snooze action
        val snoozeIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2, // Unique request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Rappel de tâche")
            .setContentText("La tâche '$taskTitle' nécessite votre attention.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("La tâche '$taskTitle' est en retard ou arrive bientôt à expiration."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(ContextCompat.getColor(context, R.color.purple_500))
            .setVibrate(VIBRATION_PATTERN)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Terminer",
                markCompletePendingIntent
            )
            .addAction(
                R.drawable.ic_snooze,
                "Reporter",
                snoozePendingIntent
            )
            .build()
    }

    /**
     * Handle the Mark Complete action from notification.
     */
    private fun handleMarkComplete(context: Context, taskId: Long, notificationId: Int) {
        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // TODO: Mark task as complete in database using taskId
        // This would require access to the repository, which could be done via:
        // 1. A background service/worker
        // 2. Direct database access here
        // 3. Broadcasting to the app if it's running
        Log.d("TaskNotificationReceiver", "Mark complete action for task $taskId")
    }

    /**
     * Handle the Snooze action from notification.
     * Reschedules the notification for 15 minutes later.
     */
    private fun handleSnooze(context: Context, taskId: Long, taskTitle: String, notificationId: Int) {
        // Cancel the current notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // Reschedule for 15 minutes later
        val snoozeTimeMillis = System.currentTimeMillis() + (15 * 60 * 1000)
        NotificationScheduler.scheduleTaskReminder(
            context = context,
            taskId = taskId,
            taskTitle = taskTitle,
            reminderTimeMillis = snoozeTimeMillis
        )
    }
}
