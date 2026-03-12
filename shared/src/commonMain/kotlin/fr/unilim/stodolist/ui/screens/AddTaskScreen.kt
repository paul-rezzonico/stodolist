package fr.unilim.stodolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.currentTimeMillis
import fr.unilim.stodolist.ui.theme.CornerRadius
import fr.unilim.stodolist.ui.theme.GlassCard
import fr.unilim.stodolist.ui.theme.Spacing
import fr.unilim.stodolist.ui.theme.glassBackgroundColor

// =============================================================================
// Constants
// =============================================================================

private const val MAX_TITLE_LENGTH = 100
private const val MAX_DESCRIPTION_LENGTH = 500

// =============================================================================
// AddTaskScreen - Main Composable
// =============================================================================

/**
 * A modern, beautifully designed composable screen for adding a new task.
 *
 * Features:
 * - Glass-like app bar with gradient background
 * - Modern form layout with GlassCard containers
 * - Material3 DatePicker dialog for due date selection
 * - Character counters and validation
 * - Loading state and success animation
 * - Proper spacing and visual hierarchy
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
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    // Validation state
    var titleError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var showDateWarning by remember { mutableStateOf(false) }
    
    // UI state
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Focus management
    val titleFocusRequester = remember { FocusRequester() }
    
    // Form validity check
    val isFormValid by remember {
        derivedStateOf {
            title.isNotBlank() && title.length <= MAX_TITLE_LENGTH
        }
    }
    
    // Request focus on title field when screen opens
    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }
    
    // Check for past date warning
    LaunchedEffect(selectedDateMillis) {
        showDateWarning = selectedDateMillis?.let { 
            isDateInPast(it) 
        } ?: false
    }
    
    // Background gradient matching TaskListScreen
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )
    
    // App bar gradient
    val appBarGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            glassBackgroundColor()
        )
    )
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ModernAddTaskAppBar(
                onBackClick = onCancel,
                onSaveClick = {
                    if (isFormValid && !isLoading) {
                        isLoading = true
                        val descriptionValue = description.ifBlank { null }
                        onSave(title.trim(), descriptionValue, selectedDateMillis)
                    } else {
                        hasAttemptedSubmit = true
                        if (title.isBlank()) {
                            titleError = "Title is required"
                        }
                    }
                },
                isSaveEnabled = isFormValid && !isLoading,
                isLoading = isLoading,
                backgroundBrush = appBarGradient
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Title Section
                TitleFieldSection(
                    title = title,
                    onTitleChange = { newTitle ->
                        if (newTitle.length <= MAX_TITLE_LENGTH) {
                            title = newTitle
                            titleError = if (hasAttemptedSubmit && newTitle.isBlank()) {
                                "Title is required"
                            } else {
                                null
                            }
                        }
                    },
                    titleError = titleError,
                    focusRequester = titleFocusRequester
                )
                
                // Description Section
                DescriptionFieldSection(
                    description = description,
                    onDescriptionChange = { newDesc ->
                        if (newDesc.length <= MAX_DESCRIPTION_LENGTH) {
                            description = newDesc
                        }
                    }
                )
                
                // Date Picker Section
                DatePickerSection(
                    selectedDateMillis = selectedDateMillis,
                    showWarning = showDateWarning,
                    onDateClick = { showDatePicker = true },
                    onClearDate = { selectedDateMillis = null }
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                // Action Buttons Section
                ActionButtonsSection(
                    isFormValid = isFormValid,
                    isLoading = isLoading,
                    onSave = {
                        if (isFormValid && !isLoading) {
                            hasAttemptedSubmit = true
                            isLoading = true
                            val descriptionValue = description.ifBlank { null }
                            onSave(title.trim(), descriptionValue, selectedDateMillis)
                        } else {
                            hasAttemptedSubmit = true
                            if (title.isBlank()) {
                                titleError = "Title is required"
                            }
                        }
                    },
                    onCancel = onCancel
                )
                
                // Extra space for bottom
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
            
            // Success Animation Overlay
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                SuccessAnimation()
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        ModernDatePickerDialog(
            initialDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { dateMillis ->
                selectedDateMillis = dateMillis
                showDatePicker = false
            }
        )
    }
}

// =============================================================================
// Modern App Bar
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernAddTaskAppBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean,
    isLoading: Boolean,
    backgroundBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Gradient background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundBrush)
        )
        
        TopAppBar(
            title = {
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            actions = {
                // Save action in app bar
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = Spacing.sm),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = isSaveEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save task",
                            tint = if (isSaveEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

// =============================================================================
// Title Field Section
// =============================================================================

@Composable
private fun TitleFieldSection(
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    // Animated border color based on focus and error state
    val borderColor by animateColorAsState(
        targetValue = when {
            titleError != null -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "title_border_color"
    )
    
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Task Title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "*",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Title TextField
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "What do you need to do?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = title.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { onTitleChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear title",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                isError = titleError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(CornerRadius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                ),
                interactionSource = interactionSource
            )
            
            // Character counter and error message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xxs),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Error message
                AnimatedVisibility(visible = titleError != null) {
                    Text(
                        text = titleError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Character counter
                Text(
                    text = "${title.length}/$MAX_TITLE_LENGTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (title.length > MAX_TITLE_LENGTH * 0.9) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

// =============================================================================
// Description Field Section
// =============================================================================

@Composable
private fun DescriptionFieldSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "description_border_color"
    )
    
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(Optional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Description TextField
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Add more details about this task...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                shape = RoundedCornerShape(CornerRadius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                ),
                interactionSource = interactionSource
            )
            
            // Character counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xxs),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${description.length}/$MAX_DESCRIPTION_LENGTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (description.length > MAX_DESCRIPTION_LENGTH * 0.9) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

// =============================================================================
// Date Picker Section
// =============================================================================

@Composable
private fun DatePickerSection(
    selectedDateMillis: Long?,
    showWarning: Boolean,
    onDateClick: () -> Unit,
    onClearDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Due Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(Optional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Date Picker Field
            DatePickerField(
                selectedDateMillis = selectedDateMillis,
                onClick = onDateClick,
                onClear = onClearDate
            )
            
            // Past date warning
            AnimatedVisibility(visible = showWarning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "This date is in the past",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * A custom clickable field that displays the selected date or a placeholder.
 */
@Composable
private fun DatePickerField(
    selectedDateMillis: Long?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasDate = selectedDateMillis != null
    
    val backgroundColor by animateColorAsState(
        targetValue = if (hasDate) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "date_field_bg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (hasDate) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "date_field_border"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(CornerRadius.small)
            )
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (hasDate) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
            
            Text(
                text = selectedDateMillis?.let { formatDateForDisplay(it) } 
                    ?: "Select a due date",
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasDate) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
        
        // Clear button
        AnimatedVisibility(
            visible = hasDate,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear date",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================================
// Date Picker Dialog
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(datePickerState.selectedDateMillis) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true
        )
    }
}

// =============================================================================
// Action Buttons Section
// =============================================================================

@Composable
private fun ActionButtonsSection(
    isFormValid: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saveButtonScale by animateFloatAsState(
        targetValue = if (isFormValid && !isLoading) 1f else 0.97f,
        animationSpec = tween(150),
        label = "save_button_scale"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Primary Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(saveButtonScale),
            enabled = isFormValid && !isLoading,
            shape = RoundedCornerShape(CornerRadius.medium),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Save Task",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Secondary Cancel Button
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// Success Animation
// =============================================================================

@Composable
private fun SuccessAnimation(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

// =============================================================================
// Helper Functions
// =============================================================================

/**
 * Checks if a timestamp represents a date in the past.
 */
private fun isDateInPast(timestamp: Long): Boolean {
    val now = currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val todayStart = (now / oneDayMs) * oneDayMs
    return timestamp < todayStart
}

/**
 * Formats a Unix timestamp for display.
 * Returns "Today", "Tomorrow", or full date format like "Mon, January 15, 2024".
 */
private fun formatDateForDisplay(timestamp: Long): String {
    val now = currentTimeMillis()
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
 * Formats a Unix timestamp to "Mon, January 15, 2024" format.
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
    
    val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    var month = 0
    while (month < 12 && remainingDays >= daysInMonths[month]) {
        remainingDays -= daysInMonths[month]
        month++
    }
    
    val day = remainingDays + 1
    
    // Calculate day of week (0 = Thursday for epoch)
    val dayOfWeek = ((totalDays + 4) % 7) // 0 = Sun, 1 = Mon, etc.
    val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    return "${dayNames[dayOfWeek]}, ${monthNames[month]} $day, $year"
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
