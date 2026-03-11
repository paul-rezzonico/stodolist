package fr.unilim.stodolist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Task

/**
 * Formats a Unix timestamp (milliseconds) to a readable date string.
 * Uses a simple format: YYYY-MM-DD
 */
private fun formatDate(timestamp: Long): String {
    // Simple date formatting without platform-specific dependencies
    val totalSeconds = timestamp / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24
    
    // Calculate year, month, day from epoch (1970-01-01)
    // This is a simplified calculation
    var remainingDays = totalDays.toInt()
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
    
    var month = 0
    while (month < 12 && remainingDays >= daysInMonths[month]) {
        remainingDays -= daysInMonths[month]
        month++
    }
    
    val day = remainingDays + 1
    month += 1 // Convert to 1-indexed
    
    return "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

/**
 * A composable that displays a single task item in a Material3 Card.
 *
 * @param task The task to display
 * @param onCheckedChange Callback when the completion checkbox is toggled
 * @param onDeleteClick Callback when the delete button is clicked
 * @param modifier Optional modifier for the Card
 */
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textDecoration by remember(task.isCompleted) {
        derivedStateOf {
            if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        }
    }
    
    val textColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    textDecoration = textDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                task.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.8f),
                            textDecoration = textDecoration,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                task.dueDate?.let { dueDate ->
                    Text(
                        text = "Due: ${formatDate(dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
