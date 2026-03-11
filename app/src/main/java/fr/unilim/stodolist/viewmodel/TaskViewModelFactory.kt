@file:Suppress("DEPRECATION")

package fr.unilim.stodolist.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.unilim.stodolist.database.DatabaseDriverFactory
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.repository.TaskRepositoryImpl

/**
 * Factory for creating TaskViewModel instances.
 * 
 * Handles the creation of the entire dependency chain:
 * - DatabaseDriverFactory (platform-specific)
 * - StodolistDatabase (SQLDelight)
 * - TaskRepositoryImpl
 * - CommonTaskViewModel (shared)
 * - TaskViewModel (Android wrapper)
 *
 * @property application Android application context for database creation
 * 
 * @deprecated This factory is deprecated. The app now creates the dependency chain
 * directly in [fr.unilim.stodolist.ui.MainActivity] using CommonTaskViewModel from the shared module.
 * This file is kept for backward compatibility with deprecated Fragment-based UI.
 */
@Deprecated(
    message = "Use CommonTaskViewModel from shared module directly. See MainActivity.",
    level = DeprecationLevel.WARNING
)
class TaskViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            // Create the database driver using the Android context
            val driverFactory = DatabaseDriverFactory(application.applicationContext)
            val driver = driverFactory.createDriver()
            
            // Create the SQLDelight database
            val database = StodolistDatabase(driver)
            
            // Create the repository implementation
            val repository = TaskRepositoryImpl(database)
            
            // Create the common ViewModel with a scope that will be provided by the Android ViewModel
            // We'll use a custom scope that delegates to viewModelScope
            val commonViewModel = CommonTaskViewModel(
                repository = repository,
                scope = kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()
                )
            )
            
            // Create and return the Android ViewModel wrapper
            return TaskViewModel(commonViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
