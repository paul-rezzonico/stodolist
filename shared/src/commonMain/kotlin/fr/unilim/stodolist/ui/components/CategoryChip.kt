package fr.unilim.stodolist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import fr.unilim.stodolist.ui.theme.IconSize
import fr.unilim.stodolist.ui.theme.Spacing

// =============================================================================
// Color Utility
// =============================================================================

/**
 * Converts a hex color string to a Compose Color.
 * Supports formats: "#RRGGBB" or "RRGGBB"
 *
 * @return Color parsed from the hex string
 */
fun String.toColor(): Color {
    val hex = this.removePrefix("#")
    return try {
        Color(hex.toLong(16) or 0xFF000000)
    } catch (e: Exception) {
        Color.Gray // Fallback color
    }
}

// =============================================================================
// CategoryChip Component
// =============================================================================

/**
 * A modern chip component for displaying a category with glassmorphism styling.
 *
 * Features:
 * - Displays emoji icon + category name
 * - Background color from category's colorHex with transparency
 * - Glassmorphism style matching the app theme
 * - Selectable state with animated transitions
 * - Deletable state with close button
 *
 * @param category The category to display
 * @param isSelected Whether the chip is currently selected
 * @param onSelect Callback when the chip is selected/clicked
 * @param onDelete Callback when the delete button is clicked (shows close icon if provided)
 * @param modifier Optional modifier for the component
 */
@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val categoryColor = category.colorHex.toColor()
    
    // Animated scale for selection feedback
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "chip_scale"
    )
    
    // Animated background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.3f)
        } else {
            categoryColor.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "chip_background"
    )
    
    // Animated border color
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.8f)
        } else {
            categoryColor.copy(alpha = 0.3f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "chip_border"
    )
    
    // Animated text color
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "chip_text"
    )
    
    // Glassmorphism overlay gradient
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
            .then(
                if (onSelect != null) {
                    Modifier.clickable { onSelect() }
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box {
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayBrush)
            )
            
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = Spacing.sm,
                        vertical = Spacing.xs
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
                
                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .padding(start = Spacing.xxs)
                            .size(IconSize.small)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove ${category.name}",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// CategoryChip Variants
// =============================================================================

/**
 * A compact category chip showing only the emoji icon.
 * Useful for space-constrained layouts.
 *
 * @param category The category to display
 * @param isSelected Whether the chip is currently selected
 * @param onSelect Callback when the chip is selected/clicked
 * @param modifier Optional modifier for the component
 */
@Composable
fun CompactCategoryChip(
    category: Category,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val categoryColor = category.colorHex.toColor()
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "compact_chip_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.3f)
        } else {
            categoryColor.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "compact_chip_background"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            categoryColor.copy(alpha = 0.8f)
        } else {
            categoryColor.copy(alpha = 0.3f)
        },
        animationSpec = tween(durationMillis = AnimationDuration.fast),
        label = "compact_chip_border"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .then(
                if (onSelect != null) {
                    Modifier.clickable { onSelect() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.icon,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
