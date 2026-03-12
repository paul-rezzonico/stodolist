package fr.unilim.stodolist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.unilim.stodolist.ui.screens.AddTaskScreen
import fr.unilim.stodolist.ui.screens.TaskListScreen
import fr.unilim.stodolist.ui.theme.AppTheme
import fr.unilim.stodolist.ui.theme.screenEnterTransition
import fr.unilim.stodolist.ui.theme.screenExitTransition
import fr.unilim.stodolist.ui.theme.screenPopEnterTransition
import fr.unilim.stodolist.ui.theme.screenPopExitTransition
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel

/**
 * Navigation state for the app.
 */
private sealed class Screen {
    data object TaskList : Screen()
    data object AddTask : Screen()
}

/**
 * Main App composable that serves as the entry point for the shared UI.
 * Handles simple navigation between TaskListScreen and AddTaskScreen.
 *
 * Uses smooth screen transitions with combined slide and fade effects.
 *
 * @param viewModel The CommonTaskViewModel for task operations
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun App(
    viewModel: CommonTaskViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }
    var isNavigatingForward by remember { mutableStateOf(true) }

    AppTheme {
        // Surface provides the background color from the theme
        // Note: Status bar handling should be done at the platform level
        // - Android: Use WindowCompat.setDecorFitsSystemWindows() and SystemBarStyle
        // - iOS: Configure status bar appearance in Info.plist or UIViewController
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                modifier = modifier.fillMaxSize(),
                transitionSpec = {
                    if (isNavigatingForward) {
                        // Navigate forward: slide in from right + fade, slide out to left + fade
                        screenEnterTransition togetherWith screenExitTransition
                    } else {
                        // Navigate back: slide in from left + fade, slide out to right + fade
                        screenPopEnterTransition togetherWith screenPopExitTransition
                    }
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.TaskList -> {
                        TaskListScreen(
                            viewModel = viewModel,
                            onAddTaskClick = {
                                isNavigatingForward = true
                                currentScreen = Screen.AddTask
                            }
                        )
                    }

                    is Screen.AddTask -> {
                        AddTaskScreen(
                            onSave = { title, description, dueDate ->
                                viewModel.addTask(title, description, dueDate)
                                isNavigatingForward = false
                                currentScreen = Screen.TaskList
                            },
                            onCancel = {
                                isNavigatingForward = false
                                currentScreen = Screen.TaskList
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * App composable variant that accepts a custom dark theme flag.
 *
 * Uses smooth screen transitions with combined slide and fade effects.
 *
 * @param viewModel The CommonTaskViewModel for task operations
 * @param darkTheme Whether to use dark theme
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun App(
    viewModel: CommonTaskViewModel,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }
    var isNavigatingForward by remember { mutableStateOf(true) }

    AppTheme(darkTheme = darkTheme) {
        // Surface provides the background color from the theme
        // Note: Status bar handling should be done at the platform level
        // - Android: Use WindowCompat.setDecorFitsSystemWindows() and SystemBarStyle
        // - iOS: Configure status bar appearance in Info.plist or UIViewController
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                modifier = modifier.fillMaxSize(),
                transitionSpec = {
                    if (isNavigatingForward) {
                        // Navigate forward: slide in from right + fade, slide out to left + fade
                        screenEnterTransition togetherWith screenExitTransition
                    } else {
                        // Navigate back: slide in from left + fade, slide out to right + fade
                        screenPopEnterTransition togetherWith screenPopExitTransition
                    }
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.TaskList -> {
                        TaskListScreen(
                            viewModel = viewModel,
                            onAddTaskClick = {
                                isNavigatingForward = true
                                currentScreen = Screen.AddTask
                            }
                        )
                    }

                    is Screen.AddTask -> {
                        AddTaskScreen(
                            onSave = { title, description, dueDate ->
                                viewModel.addTask(title, description, dueDate)
                                isNavigatingForward = false
                                currentScreen = Screen.TaskList
                            },
                            onCancel = {
                                isNavigatingForward = false
                                currentScreen = Screen.TaskList
                            }
                        )
                    }
                }
            }
        }
    }
}
