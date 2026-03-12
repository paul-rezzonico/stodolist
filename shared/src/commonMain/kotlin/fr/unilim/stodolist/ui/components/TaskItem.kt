package fr.unilim.stodolist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.ui.theme.GlassCard
import fr.unilim.stodolist.ui.theme.IconSize
import fr.unilim.stodolist.ui.theme.Spacing

// =============================================================================
// Due Date Status
// =============================================================================

/**
 * Enum representing the urgency status of a task's due date.
 */
enum class DueDateStatus {
    /** Task is overdue */
    Overdue,
    /** Task is due today */
    Today,
    /** Task is due within the next 3 days */
    Soon,
    /** Task is due in the future (more than 3 days) */
    Future,
    /** Task has no due date */
    None
}

// =============================================================================
// Helper Functions
// =============================================================================

/**
 * Gets the current time in milliseconds using the platform-specific implementation.
 */
private fun getCurrentTimeMillis(): Long {
    return fr.unilim.stodolist.currentTimeMillis()
}

/**
 * Determines the due date status based on the timestamp.
 *
 * @param timestamp The due date as Unix timestamp in milliseconds, or null if no due date.
 * @return The [DueDateStatus] indicating urgency level.
 */
fun getDueDateStatus(timestamp: Long?): DueDateStatus {
    if (timestamp == null) return DueDateStatus.None
    
    val now = getCurrentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val threeDaysMs = 3 * oneDayMs
    
    // Get start of today (midnight)
    val todayStart = (now / oneDayMs) * oneDayMs
    val todayEnd = todayStart + oneDayMs
    
    return when {
        timestamp < todayStart -> DueDateStatus.Overdue
        timestamp < todayEnd -> DueDateStatus.Today
        timestamp < todayStart + threeDaysMs -> DueDateStatus.Soon
        else -> DueDateStatus.Future
    }
}

/**
 * Formats a Unix timestamp (milliseconds) to a human-readable date string.
 * Format: "Mon, Jan 15" or "Today" / "Tomorrow" for near dates.
 *
 * @param timestamp The Unix timestamp in milliseconds.
 * @return A formatted date string.
 */
fun formatDueDate(timestamp: Long): String {
    val now = getCurrentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    
    val todayStart = (now / oneDayMs) * oneDayMs
    val tomorrowStart = todayStart + oneDayMs
    val tomorrowEnd = tomorrowStart + oneDayMs
    
    return when {
        timestamp >= todayStart && timestamp < tomorrowStart -> "Today"
        timestamp >= tomorrowStart && timestamp < tomorrowEnd -> "Tomorrow"
        else -> formatDateFull(timestamp)
    }
}

/**
 * Formats a Unix timestamp to "Mon, Jan 15" format.
 */
private fun formatDateFull(timestamp: Long): String {
    val totalDays = (timestamp / (24 * 60 * 60 * 1000L)).toInt()
    
    // Calculate year, month, day from epoch (1970-01-01)
    var remainingDays = totalDays
    var year = 1970
    
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        year++
    }
    
    val daysInMonths = if (isLeapYear(year)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }
    
    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    
    var month = 0
    while (month < 12 && remainingDays >= daysInMonths[month]) {
        remainingDays -= daysInMonths[month]
        month++
    }
    
    val day = remainingDays + 1
    
    // Calculate day of week (0 = Thursday for epoch)
    val dayOfWeek = ((totalDays + 4) % 7) // 0 = Sun, 1 = Mon, etc.
    val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    return "${dayNames[dayOfWeek]}, ${monthNames[month]} $day"
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// =============================================================================
// Status Indicator Colors
// =============================================================================

/**
 * Returns the indicator color based on the due date status.
 */
@Composable
private fun getStatusIndicatorColor(status: DueDateStatus): Color {
    return when (status) {
        DueDateStatus.Overdue -> MaterialTheme.colorScheme.error
        DueDateStatus.Today -> Color(0xFFFF9800) // Orange/warning
        DueDateStatus.Soon -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        DueDateStatus.Future -> Color.Transparent
        DueDateStatus.None -> Color.Transparent
    }
}

/**
 * Returns the due date text color based on the status.
 */
@Composable
private fun getDueDateTextColor(status: DueDateStatus): Color {
    return when (status) {
        DueDateStatus.Overdue -> MaterialTheme.colorScheme.error
        DueDateStatus.Today -> Color(0xFFFF9800) // Orange/warning
        DueDateStatus.Soon -> MaterialTheme.colorScheme.primary
        DueDateStatus.Future -> MaterialTheme.colorScheme.onSurfaceVariant
        DueDateStatus.None -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

// =============================================================================
// Custom Circular Checkbox
// =============================================================================

/**
 * A modern circular checkbox with animated checkmark.
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Callback when the checkbox is toggled.
 * @param modifier Optional modifier.
 */
@Composable
private fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "checkbox_background"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 200),
        label = "checkbox_border"
    )
    
    val checkmarkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "checkmark_alpha"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (checked) "Completed" else "Not completed",
            modifier = Modifier
                .size(16.dp)
                .alpha(checkmarkAlpha),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// =============================================================================
// Status Indicator Strip
// =============================================================================

/**
 * A vertical status indicator strip that shows task urgency.
 */
@Composable
private fun StatusIndicatorStrip(
    status: DueDateStatus,
    modifier: Modifier = Modifier
) {
    val indicatorColor = getStatusIndicatorColor(status)
    
    if (status != DueDateStatus.None && status != DueDateStatus.Future) {
        Box(
            modifier = modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(indicatorColor)
        )
    } else {
        Spacer(modifier = modifier.width(4.dp))
    }
}

// =============================================================================
// TaskItem Component
// =============================================================================

/**
 * A modern glassmorphism-styled task item component.
 *
 * Features:
 * - GlassCard container with frosted glass effect
 * - Visual status indicators (overdue, today, soon)
 * - Custom circular checkbox with animations
 * - Proper typography and spacing
 * - Completion state animations
 *
 * @param task The task to display.
 * @param onCheckedChange Callback when the completion checkbox is toggled.
 * @param onDeleteClick Callback when the delete button is clicked.
 * @param onClick Optional callback when the card is clicked (for editing).
 * @param modifier Optional modifier for the component.
 */
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dueDateStatus = remember(task.dueDate, task.isCompleted) {
        if (task.isCompleted) DueDateStatus.None else getDueDateStatus(task.dueDate)
    }
    
    // Animate overall alpha for completed state
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "content_alpha"
    )
    
    // Text styling based on completion
    val titleTextDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val titleFontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold
    
    val textColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )
    
    val secondaryTextColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "secondary_text_color"
    )
    
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .alpha(contentAlpha),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (task.description.isNullOrBlank()) 64.dp else 80.dp)
                .padding(end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator strip
            StatusIndicatorStrip(
                status = dueDateStatus,
                modifier = Modifier.fillMaxHeight()
            )
            
            Spacer(modifier = Modifier.width(Spacing.sm))
            
            // Custom circular checkbox
            CircularCheckbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange
            )
            
            Spacer(modifier = Modifier.width(Spacing.sm))
            
            // Content column: title, description, due date
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = titleFontWeight,
                    color = textColor,
                    textDecoration = titleTextDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description (if present)
                task.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Due date (if present and not completed)
                if (!task.isCompleted) {
                    task.dueDate?.let { dueDate ->
                        val dueDateColor = getDueDateTextColor(dueDateStatus)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.small),
                                tint = dueDateColor
                            )
                            Text(
                                text = formatDueDate(dueDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = dueDateColor
                            )
                        }
                    }
                }
            }
            
            // Delete action button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    modifier = Modifier.size(IconSize.medium),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
