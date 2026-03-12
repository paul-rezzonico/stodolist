package fr.unilim.stodolist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
 * Uses Jetpack Compose with PermissionScreen that wraps the App composable.
 * Permission handling is delegated to the new permission system.
 */
class MainActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the dependency chain
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val driver = driverFactory.createDriver()
        val database = StodolistDatabase(driver)
        val repository = TaskRepositoryImpl(database)
        val viewModel = CommonTaskViewModel(
            repository = repository,
            scope = activityScope
        )

        // Set Compose content using PermissionScreen which wraps App
        setContent {
            PermissionScreen(viewModel = viewModel)
        }
    }
}
