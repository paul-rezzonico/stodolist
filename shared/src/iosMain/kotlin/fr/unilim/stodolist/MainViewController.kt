package fr.unilim.stodolist

import androidx.compose.ui.window.ComposeUIViewController
import fr.unilim.stodolist.database.DatabaseDriverFactory
import fr.unilim.stodolist.database.StodolistDatabase
import fr.unilim.stodolist.repository.TaskRepositoryImpl
import fr.unilim.stodolist.ui.App
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel
import kotlinx.coroutines.MainScope
import platform.UIKit.UIViewController

/**
 * Creates the main UIViewController for the iOS app.
 * This function serves as the entry point from Swift/SwiftUI to the Compose UI.
 *
 * Sets up the complete dependency chain:
 * - DatabaseDriverFactory for iOS SQLite driver
 * - StodolistDatabase instance
 * - TaskRepositoryImpl for data operations
 * - CommonTaskViewModel with MainScope for coroutines
 *
 * @return UIViewController containing the Compose UI
 */
fun MainViewController(): UIViewController {
    // Create the dependency chain
    val driverFactory = DatabaseDriverFactory()
    val database = StodolistDatabase(driverFactory.createDriver())
    val repository = TaskRepositoryImpl(database)
    val viewModel = CommonTaskViewModel(repository, MainScope())

    return ComposeUIViewController {
        App(viewModel = viewModel)
    }
}
