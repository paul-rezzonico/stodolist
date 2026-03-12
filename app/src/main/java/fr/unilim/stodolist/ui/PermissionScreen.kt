package fr.unilim.stodolist.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.unilim.stodolist.permissions.NotificationPermissionHandler
import fr.unilim.stodolist.permissions.PermissionState
import fr.unilim.stodolist.permissions.PermissionStatus
import fr.unilim.stodolist.permissions.PermissionUiState
import fr.unilim.stodolist.ui.components.NotificationPermissionBanner
import fr.unilim.stodolist.ui.components.PermissionRationaleDialog
import fr.unilim.stodolist.ui.components.PermissionSettingsDialog
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel

/**
 * A wrapper composable that handles permission requests and displays
 * appropriate UI elements based on the permission state.
 * 
 * This screen wraps the main App composable and shows:
 * - Rationale dialog when shouldShowRequestPermissionRationale is true
 * - Settings redirect dialog when permission is permanently denied
 * - Denied banner at top of app when permission is denied but dismissible
 *
 * @param viewModel The CommonTaskViewModel for task operations
 * @param modifier Optional modifier
 */
@Composable
fun PermissionScreen(
    viewModel: CommonTaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission handler
    val permissionHandler = remember {
        NotificationPermissionHandler(context)
    }

    // Track if we've requested permission before
    var hasRequestedPermission by remember { mutableStateOf(false) }

    // UI state for permissions
    var permissionUiState by remember {
        mutableStateOf(
            PermissionUiState(
                notificationPermissionGranted = permissionHandler.isNotificationPermissionGranted(),
                alarmPermissionGranted = permissionHandler.isAlarmPermissionGranted()
            )
        )
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRequestedPermission = true
        
        if (isGranted) {
            permissionUiState = permissionUiState.copy(
                notificationPermissionGranted = true,
                showRationaleDialog = false,
                showSettingsDialog = false,
                showDeniedBanner = false
            )
        } else {
            // Check if we should show rationale or settings
            val status = permissionHandler.checkNotificationPermissionStatus()
            permissionUiState = when (status) {
                PermissionStatus.ShouldShowRationale -> {
                    permissionUiState.copy(
                        notificationPermissionGranted = false,
                        showRationaleDialog = true,
                        showSettingsDialog = false,
                        showDeniedBanner = false
                    )
                }
                PermissionStatus.Denied -> {
                    // Permission denied without rationale = permanently denied
                    permissionUiState.copy(
                        notificationPermissionGranted = false,
                        showRationaleDialog = false,
                        showSettingsDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                }
                else -> permissionUiState.copy(
                    notificationPermissionGranted = status == PermissionStatus.Granted ||
                            status == PermissionStatus.NotRequired
                )
            }
        }
    }

    // Check permissions when app resumes (user might return from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val notificationGranted = permissionHandler.isNotificationPermissionGranted()
                val alarmGranted = permissionHandler.isAlarmPermissionGranted()

                permissionUiState = permissionUiState.copy(
                    notificationPermissionGranted = notificationGranted,
                    alarmPermissionGranted = alarmGranted,
                    showSettingsDialog = false
                )

                // Clear banner if permission is now granted
                if (notificationGranted) {
                    permissionUiState = permissionUiState.copy(
                        showDeniedBanner = false
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial permission request
    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = permissionHandler.checkNotificationPermissionStatus()
            when (status) {
                PermissionStatus.Denied -> {
                    if (!hasRequestedPermission) {
                        // First time - request permission
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                PermissionStatus.ShouldShowRationale -> {
                    permissionUiState = permissionUiState.withRationaleDialog()
                }
                else -> { /* Already granted or not required */ }
            }
        }
        onDispose { }
    }

    // Open app settings
    val openSettings: () -> Unit = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
        permissionUiState = permissionUiState.copy(showSettingsDialog = false)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Permission denied banner (shown at top)
            NotificationPermissionBanner(
                visible = permissionUiState.showDeniedBanner && !permissionUiState.bannerDismissed,
                onGrantPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val status = permissionHandler.checkNotificationPermissionStatus()
                        if (status == PermissionStatus.ShouldShowRationale) {
                            permissionUiState = permissionUiState.withRationaleDialog()
                        } else {
                            // Likely permanently denied, open settings
                            openSettings()
                        }
                    }
                },
                onDismiss = {
                    permissionUiState = permissionUiState.dismissBanner()
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Main app content
            App(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Rationale dialog
        if (permissionUiState.showRationaleDialog) {
            PermissionRationaleDialog(
                permissionName = "Notifications",
                rationale = "Stodolist needs notification permission to remind you about upcoming tasks and deadlines. Without this permission, you won't receive task reminders.",
                onRequestPermission = {
                    permissionUiState = permissionUiState.dismissAll()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onDismiss = {
                    permissionUiState = permissionUiState.copy(
                        showRationaleDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                },
                icon = Icons.Default.Notifications
            )
        }

        // Settings redirect dialog
        if (permissionUiState.showSettingsDialog) {
            PermissionSettingsDialog(
                permissionName = "Notifications",
                message = "Notification permission has been disabled. To receive task reminders, please enable notifications in your device settings.",
                onOpenSettings = openSettings,
                onDismiss = {
                    permissionUiState = permissionUiState.copy(
                        showSettingsDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                },
                icon = Icons.Default.Notifications
            )
        }
    }
}

/**
 * PermissionScreen variant that accepts a custom dark theme flag.
 *
 * @param viewModel The CommonTaskViewModel for task operations
 * @param darkTheme Whether to use dark theme
 * @param modifier Optional modifier
 */
@Composable
fun PermissionScreen(
    viewModel: CommonTaskViewModel,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionHandler = remember {
        NotificationPermissionHandler(context)
    }

    var hasRequestedPermission by remember { mutableStateOf(false) }

    var permissionUiState by remember {
        mutableStateOf(
            PermissionUiState(
                notificationPermissionGranted = permissionHandler.isNotificationPermissionGranted(),
                alarmPermissionGranted = permissionHandler.isAlarmPermissionGranted()
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRequestedPermission = true

        if (isGranted) {
            permissionUiState = permissionUiState.copy(
                notificationPermissionGranted = true,
                showRationaleDialog = false,
                showSettingsDialog = false,
                showDeniedBanner = false
            )
        } else {
            val status = permissionHandler.checkNotificationPermissionStatus()
            permissionUiState = when (status) {
                PermissionStatus.ShouldShowRationale -> {
                    permissionUiState.copy(
                        notificationPermissionGranted = false,
                        showRationaleDialog = true,
                        showSettingsDialog = false,
                        showDeniedBanner = false
                    )
                }
                PermissionStatus.Denied -> {
                    permissionUiState.copy(
                        notificationPermissionGranted = false,
                        showRationaleDialog = false,
                        showSettingsDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                }
                else -> permissionUiState.copy(
                    notificationPermissionGranted = status == PermissionStatus.Granted ||
                            status == PermissionStatus.NotRequired
                )
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val notificationGranted = permissionHandler.isNotificationPermissionGranted()
                val alarmGranted = permissionHandler.isAlarmPermissionGranted()

                permissionUiState = permissionUiState.copy(
                    notificationPermissionGranted = notificationGranted,
                    alarmPermissionGranted = alarmGranted,
                    showSettingsDialog = false
                )

                if (notificationGranted) {
                    permissionUiState = permissionUiState.copy(showDeniedBanner = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = permissionHandler.checkNotificationPermissionStatus()
            when (status) {
                PermissionStatus.Denied -> {
                    if (!hasRequestedPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                PermissionStatus.ShouldShowRationale -> {
                    permissionUiState = permissionUiState.withRationaleDialog()
                }
                else -> { }
            }
        }
        onDispose { }
    }

    val openSettings: () -> Unit = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
        permissionUiState = permissionUiState.copy(showSettingsDialog = false)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotificationPermissionBanner(
                visible = permissionUiState.showDeniedBanner && !permissionUiState.bannerDismissed,
                onGrantPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val status = permissionHandler.checkNotificationPermissionStatus()
                        if (status == PermissionStatus.ShouldShowRationale) {
                            permissionUiState = permissionUiState.withRationaleDialog()
                        } else {
                            openSettings()
                        }
                    }
                },
                onDismiss = {
                    permissionUiState = permissionUiState.dismissBanner()
                },
                modifier = Modifier.fillMaxWidth(),
                useDarkTheme = darkTheme
            )

            App(
                viewModel = viewModel,
                darkTheme = darkTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        if (permissionUiState.showRationaleDialog) {
            PermissionRationaleDialog(
                permissionName = "Notifications",
                rationale = "Stodolist needs notification permission to remind you about upcoming tasks and deadlines. Without this permission, you won't receive task reminders.",
                onRequestPermission = {
                    permissionUiState = permissionUiState.dismissAll()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onDismiss = {
                    permissionUiState = permissionUiState.copy(
                        showRationaleDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                },
                icon = Icons.Default.Notifications
            )
        }

        if (permissionUiState.showSettingsDialog) {
            PermissionSettingsDialog(
                permissionName = "Notifications",
                message = "Notification permission has been disabled. To receive task reminders, please enable notifications in your device settings.",
                onOpenSettings = openSettings,
                onDismiss = {
                    permissionUiState = permissionUiState.copy(
                        showSettingsDialog = false,
                        showDeniedBanner = true,
                        bannerDismissed = false
                    )
                },
                icon = Icons.Default.Notifications
            )
        }
    }
}
