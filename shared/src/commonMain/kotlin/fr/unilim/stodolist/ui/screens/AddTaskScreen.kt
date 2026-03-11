package fr.unilim.stodolist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * A composable screen for adding a new task.
 *
 * @param onSave Callback when the save button is clicked with title, description, and optional due date
 * @param onCancel Callback when the cancel button or back arrow is clicked
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onSave: (title: String, description: String?, dueDate: Long?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    val isFormValid by remember {
        derivedStateOf {
            title.isNotBlank() && dateError == null
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field (required)
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    titleError = if (it.isBlank()) "Title is required" else null
                },
                label = { Text("Title *") },
                placeholder = { Text("Enter task title") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description field (optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter task description (optional)") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Due date field (optional)
            OutlinedTextField(
                value = dueDateText,
                onValueChange = { newValue ->
                    dueDateText = newValue
                    dateError = if (newValue.isNotBlank()) {
                        validateDateFormat(newValue)
                    } else {
                        null
                    }
                },
                label = { Text("Due Date") },
                placeholder = { Text("YYYY-MM-DD (optional)") },
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } } 
                    ?: { Text("Format: YYYY-MM-DD") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = "Title is required"
                            return@Button
                        }
                        
                        val dueDate = if (dueDateText.isNotBlank()) {
                            parseDateToTimestamp(dueDateText)
                        } else {
                            null
                        }
                        
                        val descriptionValue = description.ifBlank { null }
                        onSave(title.trim(), descriptionValue, dueDate)
                    },
                    enabled = isFormValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

/**
 * Validates the date format (YYYY-MM-DD).
 * Returns an error message if invalid, null if valid.
 */
private fun validateDateFormat(dateString: String): String? {
    val parts = dateString.split("-")
    if (parts.size != 3) {
        return "Invalid format. Use YYYY-MM-DD"
    }
    
    val year = parts[0].toIntOrNull()
    val month = parts[1].toIntOrNull()
    val day = parts[2].toIntOrNull()
    
    if (year == null || year < 1970 || year > 2100) {
        return "Invalid year"
    }
    
    if (month == null || month < 1 || month > 12) {
        return "Invalid month (1-12)"
    }
    
    val maxDays = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    
    if (day == null || day < 1 || day > maxDays) {
        return "Invalid day for month"
    }
    
    return null
}

/**
 * Parses a date string (YYYY-MM-DD) to Unix timestamp in milliseconds.
 */
private fun parseDateToTimestamp(dateString: String): Long? {
    val parts = dateString.split("-")
    if (parts.size != 3) return null
    
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    
    // Calculate days from epoch (1970-01-01)
    var totalDays = 0L
    
    // Add days for complete years
    for (y in 1970 until year) {
        totalDays += if (isLeapYear(y)) 366 else 365
    }
    
    // Add days for complete months in the current year
    val daysInMonths = if (isLeapYear(year)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }
    
    for (m in 0 until (month - 1)) {
        totalDays += daysInMonths[m]
    }
    
    // Add remaining days
    totalDays += (day - 1)
    
    // Convert to milliseconds
    return totalDays * 24 * 60 * 60 * 1000
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
