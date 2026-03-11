package fr.unilim.stodolist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.ui.components.TaskItem
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * A composable screen that displays a list of tasks.
 *
 * @param viewModel The CommonTaskViewModel to observe tasks from
 * @param onAddTaskClick Callback when the FAB is clicked to add a new task
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: CommonTaskViewModel,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    
    TaskListScreenContent(
        tasks = tasks,
        onAddTaskClick = onAddTaskClick,
        onTaskCheckedChange = { task, _ -> 
            viewModel.toggleTaskCompletion(task)
        },
        onTaskDeleteClick = { task ->
            viewModel.deleteTask(task)
        },
        modifier = modifier
    )
}

/**
 * Overload that accepts a StateFlow directly for more flexibility.
 *
 * @param tasksFlow The StateFlow of tasks to observe
 * @param onAddTaskClick Callback when the FAB is clicked to add a new task
 * @param onTaskCheckedChange Callback when a task's completion status is toggled
 * @param onTaskDeleteClick Callback when a task is deleted
 * @param modifier Optional modifier for the screen
 */
@Composable
fun TaskListScreen(
    tasksFlow: StateFlow<List<Task>>,
    onAddTaskClick: () -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by tasksFlow.collectAsState()
    
    TaskListScreenContent(
        tasks = tasks,
        onAddTaskClick = onAddTaskClick,
        onTaskCheckedChange = onTaskCheckedChange,
        onTaskDeleteClick = onTaskDeleteClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListScreenContent(
    tasks: List<Task>,
    onAddTaskClick: () -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "My Tasks",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task"
                )
            }
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            EmptyTasksState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = tasks,
                    key = { task -> task.id }
                ) { task ->
                    TaskItem(
                        task = task,
                        onCheckedChange = { isChecked ->
                            onTaskCheckedChange(task, isChecked)
                        },
                        onDeleteClick = {
                            onTaskDeleteClick(task)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the + button to add your first task",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
