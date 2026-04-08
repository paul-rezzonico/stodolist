package fr.unilim.stodolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Category
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.ui.components.CategoryFilterBar
import fr.unilim.stodolist.ui.components.TaskItem
import fr.unilim.stodolist.ui.components.getDueDateStatus
import fr.unilim.stodolist.ui.components.DueDateStatus
import fr.unilim.stodolist.ui.theme.GlassCard
import fr.unilim.stodolist.ui.theme.Spacing
import fr.unilim.stodolist.ui.theme.CornerRadius as ThemeCornerRadius
import fr.unilim.stodolist.viewmodel.CommonTaskViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

// =============================================================================
// TaskListScreen with ViewModel
// =============================================================================

/**
 * A composable screen that displays a list of tasks with modern glassmorphism styling.
 *
 * Features:
 * - MediumTopAppBar with gradient background
 * - Task statistics header with animated progress
 * - Category filter bar
 * - Beautiful empty state with custom illustration
 * - Collapsible ExtendedFloatingActionButton
 * - Subtle gradient background
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
    val categories by viewModel.categories.collectAsState()
    val selectedFilterCategory by viewModel.selectedFilterCategory.collectAsState()
    
    TaskListScreenContent(
        tasks = tasks,
        categories = categories,
        selectedFilterCategoryId = selectedFilterCategory,
        onFilterCategorySelect = { categoryId ->
            viewModel.setFilterCategory(categoryId)
        },
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

// =============================================================================
// TaskListScreen with StateFlow
// =============================================================================

/**
 * Overload that accepts a StateFlow directly for more flexibility.
 *
 * @param tasksFlow The StateFlow of tasks to observe
 * @param categoriesFlow The StateFlow of categories to observe
 * @param selectedFilterCategoryFlow The StateFlow of the selected filter category ID
 * @param onFilterCategorySelect Callback when a category filter is selected
 * @param onAddTaskClick Callback when the FAB is clicked to add a new task
 * @param onTaskCheckedChange Callback when a task's completion status is toggled
 * @param onTaskDeleteClick Callback when a task is deleted
 * @param modifier Optional modifier for the screen
 */
@Composable
fun TaskListScreen(
    tasksFlow: StateFlow<List<Task>>,
    categoriesFlow: StateFlow<List<Category>> = kotlinx.coroutines.flow.MutableStateFlow(emptyList()),
    selectedFilterCategoryFlow: StateFlow<Long?> = kotlinx.coroutines.flow.MutableStateFlow(null),
    onFilterCategorySelect: (Long?) -> Unit = {},
    onAddTaskClick: () -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by tasksFlow.collectAsState()
    val categories by categoriesFlow.collectAsState()
    val selectedFilterCategory by selectedFilterCategoryFlow.collectAsState()
    
    TaskListScreenContent(
        tasks = tasks,
        categories = categories,
        selectedFilterCategoryId = selectedFilterCategory,
        onFilterCategorySelect = onFilterCategorySelect,
        onAddTaskClick = onAddTaskClick,
        onTaskCheckedChange = onTaskCheckedChange,
        onTaskDeleteClick = onTaskDeleteClick,
        modifier = modifier
    )
}

// =============================================================================
// Main Screen Content
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListScreenContent(
    tasks: List<Task>,
    categories: List<Category> = emptyList(),
    selectedFilterCategoryId: Long? = null,
    onFilterCategorySelect: (Long?) -> Unit = {},
    onAddTaskClick: () -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    
    // Determine if FAB should be expanded based on scroll state
    val isFabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50
        }
    }
    
    // Filter tasks by selected category
    val filteredTasks = remember(tasks, selectedFilterCategoryId) {
        if (selectedFilterCategoryId == null) {
            tasks
        } else {
            tasks.filter { task ->
                task.categories.any { it.id == selectedFilterCategoryId }
            }
        }
    }
    
    // Calculate task statistics (based on filtered tasks for accurate display)
    val taskStats = remember(filteredTasks) {
        TaskStatistics(
            total = filteredTasks.size,
            completed = filteredTasks.count { it.isCompleted },
            overdue = filteredTasks.count { !it.isCompleted && getDueDateStatus(it.dueDate) == DueDateStatus.Overdue }
        )
    }
    
    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )
    
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ModernAppBar(
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            CollapsibleFab(
                expanded = isFabExpanded,
                onClick = onAddTaskClick
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            if (tasks.isEmpty()) {
                BeautifulEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        top = Spacing.xs,
                        bottom = Spacing.xxl + Spacing.xl // Extra space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Statistics header
                    item(key = "stats_header") {
                        TaskStatisticsHeader(stats = taskStats)
                    }
                    
                    // Category filter bar
                    if (categories.isNotEmpty()) {
                        item(key = "category_filter") {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                CategoryFilterBar(
                                    categories = categories,
                                    selectedCategoryId = selectedFilterCategoryId,
                                    onCategorySelect = onFilterCategorySelect,
                                    modifier = Modifier.padding(vertical = Spacing.xs)
                                )
                            }
                        }
                    }
                    
                    // Check if filtered results are empty
                    if (filteredTasks.isEmpty() && selectedFilterCategoryId != null) {
                        item(key = "no_results") {
                            NoFilterResultsState(
                                onClearFilter = { onFilterCategorySelect(null) }
                            )
                        }
                    } else {
                        // Active tasks section
                        val activeTasks = filteredTasks.filter { !it.isCompleted }
                        if (activeTasks.isNotEmpty()) {
                            item(key = "active_header") {
                                SectionHeader(
                                    title = "Active",
                                    count = activeTasks.size
                                )
                            }
                            
                            items(
                                items = activeTasks,
                                key = { task -> "active_${task.id}" }
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
                        
                        // Completed tasks section
                        val completedTasks = filteredTasks.filter { it.isCompleted }
                        if (completedTasks.isNotEmpty()) {
                            item(key = "completed_header") {
                                SectionHeader(
                                    title = "Completed",
                                    count = completedTasks.size
                                )
                            }
                            
                            items(
                                items = completedTasks,
                                key = { task -> "completed_${task.id}" }
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
        }
    }
}

// =============================================================================
// No Filter Results State
// =============================================================================

/**
 * Displayed when a category filter yields no results.
 */
@Composable
private fun NoFilterResultsState(
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "No tasks in this category",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.TextButton(onClick = onClearFilter) {
                Text("Show all tasks")
            }
        }
    }
}

// =============================================================================
// Task Statistics Data Class
// =============================================================================

/**
 * Data class holding task statistics for display.
 */
private data class TaskStatistics(
    val total: Int,
    val completed: Int,
    val overdue: Int
) {
    val completionPercentage: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}

// =============================================================================
// Modern App Bar
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernAppBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    )
    
    Box(modifier = modifier) {
        // Gradient background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(gradientColors)
                )
        )
        
        TopAppBar(
            title = {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            actions = {
                IconButton(onClick = { /* Search placeholder for future */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search tasks",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

// =============================================================================
// Task Statistics Header
// =============================================================================

@Composable
private fun TaskStatisticsHeader(
    stats: TaskStatistics,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = stats.completionPercentage,
        animationSpec = tween(durationMillis = 500),
        label = "progress_animation"
    )
    
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            // Task count subtitle (moved from app bar)
            Text(
                text = "${stats.total} tasks, ${stats.completed} completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total tasks
                StatisticItem(
                    value = stats.total.toString(),
                    label = "Total",
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Progress indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${(stats.completionPercentage * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(ThemeCornerRadius.small)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Overdue tasks (if any)
                if (stats.overdue > 0) {
                    StatisticItem(
                        value = stats.overdue.toString(),
                        label = "Overdue",
                        color = MaterialTheme.colorScheme.error,
                        showWarningIcon = true
                    )
                } else {
                    StatisticItem(
                        value = stats.completed.toString(),
                        label = "Done",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String,
    color: Color,
    showWarningIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            if (showWarningIcon) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================================
// Section Header
// =============================================================================

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(Spacing.xs))
        
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(ThemeCornerRadius.extraSmall)
                )
                .padding(horizontal = Spacing.xs, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =============================================================================
// Collapsible FAB
// =============================================================================

@Composable
private fun CollapsibleFab(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        },
        text = {
            Text(text = "Add Task")
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

// =============================================================================
// Beautiful Empty State
// =============================================================================

@Composable
private fun BeautifulEmptyState(
    modifier: Modifier = Modifier
) {
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating_animation")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Spacing.xl)
        ) {
            // Custom clipboard illustration using Canvas
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset { IntOffset(0, -floatingOffset.roundToInt()) }
            ) {
                ClipboardIllustration(
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.primaryContainer,
                    tertiaryColor = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Title
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Subtitle
            Text(
                text = "Tap the + button to create your first task",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
            
            // Hint arrow pointing down
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, floatingOffset.roundToInt()) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(Spacing.xxs),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// =============================================================================
// Custom Clipboard Illustration
// =============================================================================

@Composable
private fun ClipboardIllustration(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Create gradient for clipboard body
        val clipboardGradient = Brush.verticalGradient(
            colors = listOf(secondaryColor, secondaryColor.copy(alpha = 0.7f))
        )
        
        // Clipboard body (rounded rectangle)
        val clipboardPadding = width * 0.1f
        val clipboardWidth = width - (clipboardPadding * 2)
        val clipboardHeight = height * 0.85f
        val clipboardTop = height * 0.12f
        val cornerRadius = width * 0.08f
        
        // Shadow effect
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = Offset(clipboardPadding + 4f, clipboardTop + 4f),
            size = Size(clipboardWidth, clipboardHeight),
            cornerRadius = CornerRadius(cornerRadius)
        )
        
        // Clipboard body
        drawRoundRect(
            brush = clipboardGradient,
            topLeft = Offset(clipboardPadding, clipboardTop),
            size = Size(clipboardWidth, clipboardHeight),
            cornerRadius = CornerRadius(cornerRadius)
        )
        
        // Clipboard clip at top
        val clipWidth = width * 0.35f
        val clipHeight = height * 0.12f
        val clipLeft = (width - clipWidth) / 2
        
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(clipLeft, 0f),
            size = Size(clipWidth, clipHeight),
            cornerRadius = CornerRadius(width * 0.04f)
        )
        
        // Inner clip circle
        drawCircle(
            color = secondaryColor,
            radius = width * 0.06f,
            center = Offset(width / 2, clipHeight * 0.5f)
        )
        
        // Checklist lines
        val lineStartX = clipboardPadding + width * 0.18f
        val lineEndX = clipboardPadding + clipboardWidth - width * 0.1f
        val checkboxSize = width * 0.08f
        val checkboxX = clipboardPadding + width * 0.08f
        
        // First line (checked)
        val line1Y = clipboardTop + height * 0.18f
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(checkboxX, line1Y - checkboxSize / 2),
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(width * 0.015f)
        )
        // Checkmark
        val checkPath = Path().apply {
            moveTo(checkboxX + checkboxSize * 0.2f, line1Y)
            lineTo(checkboxX + checkboxSize * 0.45f, line1Y + checkboxSize * 0.25f)
            lineTo(checkboxX + checkboxSize * 0.85f, line1Y - checkboxSize * 0.25f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = width * 0.025f, cap = StrokeCap.Round)
        )
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.4f),
            topLeft = Offset(lineStartX, line1Y - height * 0.012f),
            size = Size(lineEndX - lineStartX, height * 0.024f),
            cornerRadius = CornerRadius(height * 0.012f)
        )
        
        // Second line (checked)
        val line2Y = clipboardTop + height * 0.32f
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(checkboxX, line2Y - checkboxSize / 2),
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(width * 0.015f)
        )
        val checkPath2 = Path().apply {
            moveTo(checkboxX + checkboxSize * 0.2f, line2Y)
            lineTo(checkboxX + checkboxSize * 0.45f, line2Y + checkboxSize * 0.25f)
            lineTo(checkboxX + checkboxSize * 0.85f, line2Y - checkboxSize * 0.25f)
        }
        drawPath(
            path = checkPath2,
            color = Color.White,
            style = Stroke(width = width * 0.025f, cap = StrokeCap.Round)
        )
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.4f),
            topLeft = Offset(lineStartX, line2Y - height * 0.012f),
            size = Size((lineEndX - lineStartX) * 0.7f, height * 0.024f),
            cornerRadius = CornerRadius(height * 0.012f)
        )
        
        // Third line (unchecked - empty)
        val line3Y = clipboardTop + height * 0.46f
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.3f),
            topLeft = Offset(checkboxX, line3Y - checkboxSize / 2),
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(width * 0.015f),
            style = Stroke(width = width * 0.02f)
        )
        drawRoundRect(
            color = tertiaryColor.copy(alpha = 0.5f),
            topLeft = Offset(lineStartX, line3Y - height * 0.012f),
            size = Size((lineEndX - lineStartX) * 0.85f, height * 0.024f),
            cornerRadius = CornerRadius(height * 0.012f)
        )
        
        // Fourth line (unchecked - dashed/placeholder)
        val line4Y = clipboardTop + height * 0.60f
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.2f),
            topLeft = Offset(checkboxX, line4Y - checkboxSize / 2),
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(width * 0.015f),
            style = Stroke(width = width * 0.02f)
        )
        drawRoundRect(
            color = tertiaryColor.copy(alpha = 0.3f),
            topLeft = Offset(lineStartX, line4Y - height * 0.012f),
            size = Size((lineEndX - lineStartX) * 0.5f, height * 0.024f),
            cornerRadius = CornerRadius(height * 0.012f)
        )
        
        // Plus icon hint
        val plusCenterX = width / 2
        val plusCenterY = clipboardTop + height * 0.75f
        val plusSize = width * 0.12f
        val plusStroke = width * 0.03f
        
        drawCircle(
            color = primaryColor.copy(alpha = 0.15f),
            radius = plusSize,
            center = Offset(plusCenterX, plusCenterY)
        )
        
        // Horizontal line of plus
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.5f),
            topLeft = Offset(plusCenterX - plusSize * 0.5f, plusCenterY - plusStroke / 2),
            size = Size(plusSize, plusStroke),
            cornerRadius = CornerRadius(plusStroke / 2)
        )
        
        // Vertical line of plus
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.5f),
            topLeft = Offset(plusCenterX - plusStroke / 2, plusCenterY - plusSize * 0.5f),
            size = Size(plusStroke, plusSize),
            cornerRadius = CornerRadius(plusStroke / 2)
        )
    }
}
