package fr.unilim.stodolist.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import fr.unilim.stodolist.database.DatabaseDriverFactory
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.repository.TaskRepositoryImpl
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Main Activity for the Stodolist app.
 * 
 * Uses Jetpack Compose with the shared App composable from the shared module.
 * All UI is now handled by Compose - the Fragment-based navigation has been removed.
 */
class MainActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Permission launcher for notification permission (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // Permission result handled - app continues regardless
        // Notifications will work if granted, silently fail if not
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Create the dependency chain
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val driver = driverFactory.createDriver()
        val database = StodolistDatabase(driver)
        val repository = TaskRepositoryImpl(database)
        val viewModel = CommonTaskViewModel(
            repository = repository,
            scope = activityScope
        )

        // Set Compose content using the shared App composable
        setContent {
            App(
                viewModel = viewModel
            )
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission for Android 13+ (API 33+).
     * On older versions, this permission is granted automatically.
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Could show rationale, but for simplicity just request
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
