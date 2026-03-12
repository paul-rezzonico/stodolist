package fr.unilim.stodolist.permissions

/**
 * Represents the current status of a permission.
 */
enum class PermissionStatus {
    /**
     * Permission has been granted by the user.
     */
    Granted,

    /**
     * Permission has been denied by the user.
     */
    Denied,

    /**
     * Permission has been denied and the system suggests showing a rationale
     * explaining why the permission is needed.
     */
    ShouldShowRationale,

    /**
     * Permission is not required for this Android version.
     * For example, POST_NOTIFICATIONS is not required before Android 13.
     */
    NotRequired
}

/**
 * Sealed class representing the different states a permission request can be in.
 */
sealed class PermissionState {
    /**
     * Permission has not been requested yet.
     */
    data object NotRequested : PermissionState()

    /**
     * Permission has been granted.
     */
    data object Granted : PermissionState()

    /**
     * Permission has been denied but can still be requested again.
     */
    data object Denied : PermissionState()

    /**
     * Permission has been permanently denied (user selected "Don't ask again").
     * User must be redirected to app settings to grant the permission.
     */
    data object PermanentlyDenied : PermissionState()

    /**
     * Permission has been denied and the system suggests showing a rationale.
     */
    data object ShouldShowRationale : PermissionState()
}

/**
 * Data class representing the UI state for permission handling.
 * 
 * This class encapsulates all the boolean flags needed to control
 * the visibility of permission-related UI elements.
 *
 * @property showRationaleDialog Whether to show the rationale dialog
 * @property showDeniedBanner Whether to show the permission denied banner
 * @property showSettingsDialog Whether to show the settings redirect dialog
 * @property notificationPermissionGranted Whether notification permission is granted
 * @property alarmPermissionGranted Whether exact alarm permission is granted
 * @property bannerDismissed Whether the user has dismissed the denial banner
 */
data class PermissionUiState(
    val showRationaleDialog: Boolean = false,
    val showDeniedBanner: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val alarmPermissionGranted: Boolean = false,
    val bannerDismissed: Boolean = false
) {
    /**
     * Returns true if all required permissions are granted.
     */
    val allPermissionsGranted: Boolean
        get() = notificationPermissionGranted && alarmPermissionGranted

    /**
     * Returns true if any permission dialog or banner should be shown.
     */
    val hasActivePermissionUi: Boolean
        get() = showRationaleDialog || showSettingsDialog || (showDeniedBanner && !bannerDismissed)

    /**
     * Creates a copy with the rationale dialog shown.
     */
    fun withRationaleDialog(): PermissionUiState = copy(
        showRationaleDialog = true,
        showDeniedBanner = false,
        showSettingsDialog = false
    )

    /**
     * Creates a copy with the settings dialog shown.
     */
    fun withSettingsDialog(): PermissionUiState = copy(
        showRationaleDialog = false,
        showDeniedBanner = false,
        showSettingsDialog = true
    )

    /**
     * Creates a copy with the denied banner shown.
     */
    fun withDeniedBanner(): PermissionUiState = copy(
        showRationaleDialog = false,
        showDeniedBanner = true,
        showSettingsDialog = false,
        bannerDismissed = false
    )

    /**
     * Creates a copy with all dialogs and banners hidden.
     */
    fun dismissAll(): PermissionUiState = copy(
        showRationaleDialog = false,
        showDeniedBanner = false,
        showSettingsDialog = false
    )

    /**
     * Creates a copy with the banner dismissed.
     */
    fun dismissBanner(): PermissionUiState = copy(
        bannerDismissed = true
    )

    companion object {
        /**
         * Initial state with all permissions not granted and no UI shown.
         */
        val Initial = PermissionUiState()

        /**
         * State where all permissions are granted.
         */
        val AllGranted = PermissionUiState(
            notificationPermissionGranted = true,
            alarmPermissionGranted = true
        )
    }
}
