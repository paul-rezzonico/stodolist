package fr.unilim.stodolist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.unilim.stodolist.models.Category
import fr.unilim.stodolist.ui.theme.AnimationDuration
import fr.unilim.stodolist.ui.theme.GlassConfig
import fr.unilim.stodolist.ui.theme.Spacing

// =============================================================================
// CategoryFilterBar Component
// =============================================================================

/**
 * A horizontal scrollable filter bar for filtering tasks by category.
 *
 * Features:
 * - "All" chip at the start (selected when no filter is active)
 * - Horizontally scrollable row of category chips
 * - Single selection mode (filter by one category at a time)
 * - Shows category icon + name with colored indicator when selected
 * - Glassmorphism styling matching the app theme
 *
 * @param categories List of all available categories
 * @param selectedCategoryId Currently selected category ID (null = All)
 * @param onCategorySelect Callback when a category is selected (null for All)
 * @param modifier Optional modifier for the component
 */
@Composable
fun CategoryFilterBar(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" chip
        AllFilterChip(
            isSelected = selectedCategoryId == null,
            onClick = { onCategorySelect(null) }
        )
        
        // Category chips
        categories.forEach { category ->
            CategoryFilterChip(
                category = category,
                isSelected = selectedCategoryId == category.id,
                onClick = { onCategorySelect(category.id) }
            )
        }
    }
}

// =============================================================================
// Multi-Select Category Filter Bar
// =============================================================================

/**
 * A horizontal scrollable filter bar that supports multi-selection.
 *
 * Features:
 * - "All" chip at the start (selected when no filter is active)
 * - Horizontally scrollable row of category chips
 * - Multi-selection mode (filter by multiple categories)
 * - Shows category icon + name with colored indicator when selected
 *
 * @param categories List of all available categories
 * @param selectedCategoryIds Set of currently selected category IDs
 * @param onCategoryToggle Callback when a category is toggled
 * @param onClearAll Callback to clear all selections (select All)
 * @param modifier Optional modifier for the component
 */
@Composable
fun MultiSelectCategoryFilterBar(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    onCategoryToggle: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" chip - selected when no categories are selected
        AllFilterChip(
            isSelected = selectedCategoryIds.isEmpty(),
            onClick = onClearAll
        )
        
        // Category chips
        categories.forEach { category ->
            CategoryFilterChip(
                category = category,
                isSelected = selectedCategoryIds.contains(category.id),
                onClick = { onCategoryToggle(category.id) }
            )
        }
    }
}

// =============================================================================
// "All" Filter Chip
// =============================================================================

/**
 * A chip representing the "All" filter option.
 */
@Composable
private fun AllFilterChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "all_chip_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "all_chip_background"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "all_chip_border"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "all_chip_text"
    )
    
    val overlayBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = GlassConfig.OVERLAY_ALPHA),
            Color.Transparent,
            Color.White.copy(alpha = GlassConfig.OVERLAY_ALPHA / 2)
        )
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
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
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                // Selection indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// =============================================================================
// Category Filter Chip
// =============================================================================

/**
 * A chip for filtering by a specific category.
 */
@Composable
private fun CategoryFilterChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = category.colorHex.toColor()
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "filter_chip_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "filter_chip_background"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "filter_chip_border"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "filter_chip_text"
    )
    
    val overlayBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = GlassConfig.OVERLAY_ALPHA),
            Color.Transparent,
            Color.White.copy(alpha = GlassConfig.OVERLAY_ALPHA / 2)
        )
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
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
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                // Colored indicator when selected
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                }
                
                // Category icon (emoji)
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.bodySmall
                )
                
                // Category name
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// =============================================================================
// Minimal Filter Bar Variant
// =============================================================================

/**
 * A minimal variant of the filter bar showing only icons for selected state.
 * Expands to show name on selection.
 *
 * @param categories List of all available categories
 * @param selectedCategoryId Currently selected category ID (null = All)
 * @param onCategorySelect Callback when a category is selected (null for All)
 * @param modifier Optional modifier for the component
 */
@Composable
fun MinimalCategoryFilterBar(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" indicator
        MinimalFilterIndicator(
            isSelected = selectedCategoryId == null,
            color = MaterialTheme.colorScheme.primary,
            label = "All",
            onClick = { onCategorySelect(null) }
        )
        
        // Category indicators
        categories.forEach { category ->
            MinimalFilterIndicator(
                isSelected = selectedCategoryId == category.id,
                color = category.colorHex.toColor(),
                label = category.icon,
                showLabel = true,
                onClick = { onCategorySelect(category.id) }
            )
        }
    }
}

/**
 * A minimal circular filter indicator.
 */
@Composable
private fun MinimalFilterIndicator(
    isSelected: Boolean,
    color: Color,
    label: String,
    showLabel: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "minimal_indicator_scale"
    )
    
    val indicatorWidth by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "minimal_indicator_width"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isSelected) color.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Selection indicator line below
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width((16 * indicatorWidth).dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
