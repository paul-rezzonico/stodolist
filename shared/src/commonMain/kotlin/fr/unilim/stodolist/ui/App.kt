package fr.unilim.stodolist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.unilim.stodolist.ui.screens.AddTaskScreen
import fr.unilim.stodolist.ui.screens.TaskListScreen
import fr.unilim.stodolist.ui.theme.AppTheme
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
 * @param viewModel The CommonTaskViewModel for task operations
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun App(
    viewModel: CommonTaskViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }
    
    AppTheme {
        AnimatedContent(
            targetState = currentScreen,
            modifier = modifier,
            transitionSpec = {
                if (targetState is Screen.AddTask) {
                    // Navigate forward: slide in from right
                    slideInHorizontally { width -> width } togetherWith
                        slideOutHorizontally { width -> -width }
                } else {
                    // Navigate back: slide in from left
                    slideInHorizontally { width -> -width } togetherWith
                        slideOutHorizontally { width -> width }
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.TaskList -> {
                    TaskListScreen(
                        viewModel = viewModel,
                        onAddTaskClick = {
                            currentScreen = Screen.AddTask
                        }
                    )
                }
                
                is Screen.AddTask -> {
                    AddTaskScreen(
                        onSave = { title, description, dueDate ->
                            viewModel.addTask(title, description, dueDate)
                            currentScreen = Screen.TaskList
                        },
                        onCancel = {
                            currentScreen = Screen.TaskList
                        }
                    )
                }
            }
        }
    }
}

/**
 * App composable variant that accepts a custom dark theme flag.
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
    
    AppTheme(darkTheme = darkTheme) {
        AnimatedContent(
            targetState = currentScreen,
            modifier = modifier,
            transitionSpec = {
                if (targetState is Screen.AddTask) {
                    slideInHorizontally { width -> width } togetherWith
                        slideOutHorizontally { width -> -width }
                } else {
                    slideInHorizontally { width -> -width } togetherWith
                        slideOutHorizontally { width -> width }
                }
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.TaskList -> {
                    TaskListScreen(
                        viewModel = viewModel,
                        onAddTaskClick = {
                            currentScreen = Screen.AddTask
                        }
                    )
                }
                
                is Screen.AddTask -> {
                    AddTaskScreen(
                        onSave = { title, description, dueDate ->
                            viewModel.addTask(title, description, dueDate)
                            currentScreen = Screen.TaskList
                        },
                        onCancel = {
                            currentScreen = Screen.TaskList
                        }
                    )
                }
            }
        }
    }
}
