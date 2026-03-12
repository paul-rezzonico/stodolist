package fr.unilim.stodolist.permissions

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Handler class for managing notification and alarm permissions on Android.
 * 
 * This class provides methods to check permission status and determine
 * the appropriate UI flow for requesting permissions.
 *
 * @param context The Android context
 * @param activity Optional activity for checking shouldShowRequestPermissionRationale
 */
class NotificationPermissionHandler(
    private val context: Context,
    private val activity: Activity? = null
) {
    /**
     * Checks the current status of the POST_NOTIFICATIONS permission.
     * 
     * @return The current permission status
     */
    fun checkNotificationPermissionStatus(): PermissionStatus {
        // POST_NOTIFICATIONS is only required on Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PermissionStatus.NotRequired
        }

        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )

        return when {
            permissionState == PackageManager.PERMISSION_GRANTED -> {
                PermissionStatus.Granted
            }
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                PermissionStatus.ShouldShowRationale
            }
            else -> {
                PermissionStatus.Denied
            }
        }
    }

    /**
     * Checks if notification permission is granted.
     * 
     * @return true if permission is granted or not required, false otherwise
     */
    fun isNotificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true // Not required before Android 13
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks the current status of the SCHEDULE_EXACT_ALARM permission.
     * 
     * @return The current permission status
     */
    fun checkAlarmPermissionStatus(): PermissionStatus {
        // SCHEDULE_EXACT_ALARM is only required on Android 12+ (S)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return PermissionStatus.NotRequired
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        return if (alarmManager.canScheduleExactAlarms()) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied
        }
    }

    /**
     * Checks if exact alarm permission is granted.
     * 
     * @return true if permission is granted or not required, false otherwise
     */
    fun isAlarmPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true // Not required before Android 12
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    /**
     * Determines the current PermissionState based on the permission status
     * and whether the permission has been requested before.
     * 
     * @param permissionStatus The current permission status
     * @param hasRequestedBefore Whether the permission has been requested before
     * @return The appropriate PermissionState
     */
    fun determinePermissionState(
        permissionStatus: PermissionStatus,
        hasRequestedBefore: Boolean
    ): PermissionState {
        return when (permissionStatus) {
            PermissionStatus.Granted -> PermissionState.Granted
            PermissionStatus.NotRequired -> PermissionState.Granted
            PermissionStatus.ShouldShowRationale -> PermissionState.ShouldShowRationale
            PermissionStatus.Denied -> {
                if (hasRequestedBefore) {
                    // If we've requested before and it's denied without rationale,
                    // the user likely selected "Don't ask again"
                    PermissionState.PermanentlyDenied
                } else {
                    PermissionState.NotRequested
                }
            }
        }
    }

    /**
     * Creates a PermissionUiState based on the current permission statuses.
     * 
     * @param notificationStatus The notification permission status
     * @param alarmStatus The alarm permission status
     * @param hasRequestedNotificationBefore Whether notification permission was requested before
     * @param hasRequestedAlarmBefore Whether alarm permission was requested before
     * @return The appropriate PermissionUiState
     */
    fun createUiState(
        notificationStatus: PermissionStatus = checkNotificationPermissionStatus(),
        alarmStatus: PermissionStatus = checkAlarmPermissionStatus(),
        hasRequestedNotificationBefore: Boolean = false,
        hasRequestedAlarmBefore: Boolean = false
    ): PermissionUiState {
        val notificationGranted = notificationStatus == PermissionStatus.Granted ||
                notificationStatus == PermissionStatus.NotRequired
        val alarmGranted = alarmStatus == PermissionStatus.Granted ||
                alarmStatus == PermissionStatus.NotRequired

        val notificationState = determinePermissionState(
            notificationStatus,
            hasRequestedNotificationBefore
        )

        return when {
            notificationGranted && alarmGranted -> {
                PermissionUiState.AllGranted
            }
            notificationState == PermissionState.ShouldShowRationale -> {
                PermissionUiState(
                    showRationaleDialog = true,
                    notificationPermissionGranted = notificationGranted,
                    alarmPermissionGranted = alarmGranted
                )
            }
            notificationState == PermissionState.PermanentlyDenied -> {
                PermissionUiState(
                    showSettingsDialog = true,
                    notificationPermissionGranted = notificationGranted,
                    alarmPermissionGranted = alarmGranted
                )
            }
            !notificationGranted || !alarmGranted -> {
                PermissionUiState(
                    showDeniedBanner = hasRequestedNotificationBefore || hasRequestedAlarmBefore,
                    notificationPermissionGranted = notificationGranted,
                    alarmPermissionGranted = alarmGranted
                )
            }
            else -> PermissionUiState.Initial
        }
    }

    /**
     * Checks if the POST_NOTIFICATIONS permission should be requested.
     * Returns true if on Android 13+ and permission is not granted.
     */
    fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isNotificationPermissionGranted()
    }

    /**
     * Checks if the exact alarm permission should be requested.
     * Returns true if on Android 12+ and permission is not granted.
     */
    fun shouldRequestAlarmPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !isAlarmPermissionGranted()
    }

    companion object {
        /**
         * The notification permission string.
         * Only available on Android 13+.
         */
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS

        /**
         * Checks if notification permission is required for the current Android version.
         */
        fun isNotificationPermissionRequired(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }

        /**
         * Checks if exact alarm permission is required for the current Android version.
         */
        fun isAlarmPermissionRequired(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }
    }
}
