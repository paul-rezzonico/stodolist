package fr.unilim.stodolist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Category
import fr.unilim.stodolist.ui.theme.AnimationDuration
import fr.unilim.stodolist.ui.theme.GlassConfig
import fr.unilim.stodolist.ui.theme.IconSize
import fr.unilim.stodolist.ui.theme.Spacing

// =============================================================================
// CategorySelector Component
// =============================================================================

/**
 * A component for selecting multiple categories with FlowRow layout.
 *
 * Features:
 * - Shows all available categories as chips in a wrapping FlowRow
 * - Selected categories are highlighted
 * - Optional "Add new" button for custom categories
 * - Optional filter bar at the top
 * - Supports compact and expanded modes
 *
 * @param categories List of all available categories
 * @param selectedCategoryIds Set of currently selected category IDs
 * @param onCategoryToggle Callback when a category is toggled
 * @param onAddNew Optional callback for adding a new category
 * @param showFilter Whether to show the search/filter bar
 * @param expanded Whether to show in expanded mode (more padding)
 * @param modifier Optional modifier for the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    onCategoryToggle: (Long) -> Unit,
    onAddNew: (() -> Unit)? = null,
    showFilter: Boolean = false,
    expanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    var filterText by remember { mutableStateOf("") }
    
    // Filter categories based on search text
    val filteredCategories = remember(categories, filterText) {
        if (filterText.isBlank()) {
            categories
        } else {
            categories.filter { category ->
                category.name.contains(filterText, ignoreCase = true)
            }
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Optional filter bar
        AnimatedVisibility(
            visible = showFilter,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CategoryFilterTextField(
                value = filterText,
                onValueChange = { filterText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xs)
            )
        }
        
        // Categories FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            filteredCategories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategoryIds.contains(category.id),
                    onSelect = { onCategoryToggle(category.id) }
                )
            }
            
            // Add new button
            if (onAddNew != null) {
                AddCategoryChip(
                    onClick = onAddNew,
                    expanded = expanded
                )
            }
        }
    }
}

// =============================================================================
// Filter TextField
// =============================================================================

/**
 * A styled filter text field for category search.
 */
@Composable
private fun CategoryFilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val overlayBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = GlassConfig.OVERLAY_ALPHA),
            Color.Transparent
        )
    )
    
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = GlassConfig.BORDER_ALPHA)
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayBrush)
            )
            
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.small),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Search categories...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// =============================================================================
// Add Category Chip
// =============================================================================

/**
 * A chip-styled button for adding a new category.
 */
@Composable
private fun AddCategoryChip(
    onClick: () -> Unit,
    expanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val iconTint = MaterialTheme.colorScheme.primary
    
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = Spacing.sm,
                    vertical = Spacing.xs
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add category",
                modifier = Modifier.size(IconSize.small),
                tint = iconTint
            )
            
            if (expanded) {
                Text(
                    text = "Add new",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = iconTint
                )
            }
        }
    }
}

// =============================================================================
// Compact Category Selector
// =============================================================================

/**
 * A compact variant of CategorySelector using circular chips.
 * Useful for space-constrained layouts.
 *
 * @param categories List of all available categories
 * @param selectedCategoryIds Set of currently selected category IDs
 * @param onCategoryToggle Callback when a category is toggled
 * @param modifier Optional modifier for the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactCategorySelector(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    onCategoryToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        categories.forEach { category ->
            CompactCategoryChip(
                category = category,
                isSelected = selectedCategoryIds.contains(category.id),
                onSelect = { onCategoryToggle(category.id) }
            )
        }
    }
}
