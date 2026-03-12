package fr.unilim.stodolist.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Glassmorphism Configuration
// =============================================================================

/**
 * Configuration object for glassmorphism styling parameters.
 */
object GlassConfig {
    /** Background alpha for light mode glass effect */
    const val LIGHT_MODE_ALPHA = 0.7f
    
    /** Background alpha for dark mode glass effect */
    const val DARK_MODE_ALPHA = 0.5f
    
    /** Border alpha for subtle glass border */
    const val BORDER_ALPHA = 0.1f
    
    /** Overlay alpha for simulated blur highlight */
    const val OVERLAY_ALPHA = 0.05f
    
    /** Default border width for glass elements */
    val DEFAULT_BORDER_WIDTH = 1.dp
    
    /** Default corner radius for glass elements */
    val DEFAULT_CORNER_RADIUS = 16.dp
}

// =============================================================================
// Glass Background Modifier
// =============================================================================

/**
 * Applies a glassmorphism background effect to any composable.
 * 
 * This modifier creates a semi-transparent background with a subtle border
 * to simulate the frosted glass effect commonly seen in modern UI design.
 *
 * @param isDarkTheme Whether dark theme is active. Used to adjust alpha values.
 * @param backgroundColor The base color for the glass effect. Defaults to surface color.
 * @param borderColor The color for the subtle border. Defaults to outline color.
 * @param shape The shape of the glass background.
 * @param borderWidth The width of the subtle border.
 * @return Modifier with glass effect applied.
 */
@Composable
fun Modifier.glassBackground(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shape: Shape = RoundedCornerShape(GlassConfig.DEFAULT_CORNER_RADIUS),
    borderWidth: Dp = GlassConfig.DEFAULT_BORDER_WIDTH
): Modifier {
    val alpha = if (isDarkTheme) GlassConfig.DARK_MODE_ALPHA else GlassConfig.LIGHT_MODE_ALPHA
    
    return this
        .clip(shape)
        .background(backgroundColor.copy(alpha = alpha), shape)
        .border(
            width = borderWidth,
            color = borderColor.copy(alpha = GlassConfig.BORDER_ALPHA),
            shape = shape
        )
}

// =============================================================================
// GlassCard Composable
// =============================================================================

/**
 * A Card with glassmorphism styling - semi-transparent background with
 * simulated blur effect using alpha overlays.
 *
 * @param modifier Modifier for the card.
 * @param shape The shape of the card. Defaults to medium rounded corners.
 * @param onClick Optional click handler for the card.
 * @param content The content inside the card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val alpha = if (isDarkTheme) GlassConfig.DARK_MODE_ALPHA else GlassConfig.LIGHT_MODE_ALPHA
    val glassColor = surfaceColor.copy(alpha = alpha)
    
    // Create a subtle gradient overlay to simulate light refraction
    val overlayBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = GlassConfig.OVERLAY_ALPHA),
            Color.Transparent,
            primaryColor.copy(alpha = GlassConfig.OVERLAY_ALPHA / 2)
        )
    )
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = glassColor
            ),
            border = BorderStroke(
                width = GlassConfig.DEFAULT_BORDER_WIDTH,
                color = outlineColor.copy(alpha = GlassConfig.BORDER_ALPHA)
            )
        ) {
            Box {
                // Overlay for simulated blur/light effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(overlayBrush)
                )
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = glassColor
            ),
            border = BorderStroke(
                width = GlassConfig.DEFAULT_BORDER_WIDTH,
                color = outlineColor.copy(alpha = GlassConfig.BORDER_ALPHA)
            )
        ) {
            Box {
                // Overlay for simulated blur/light effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(overlayBrush)
                )
                content()
            }
        }
    }
}

// =============================================================================
// GlassSurface Composable
// =============================================================================

/**
 * A Surface with glassmorphism styling - semi-transparent background with
 * subtle border for the frosted glass effect.
 *
 * @param modifier Modifier for the surface.
 * @param shape The shape of the surface. Defaults to medium rounded corners.
 * @param tonalElevation The tonal elevation of the surface.
 * @param content The content inside the surface.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val alpha = if (isDarkTheme) GlassConfig.DARK_MODE_ALPHA else GlassConfig.LIGHT_MODE_ALPHA
    val glassColor = surfaceColor.copy(alpha = alpha)
    
    // Create a subtle gradient overlay to simulate light refraction
    val overlayBrush = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = GlassConfig.OVERLAY_ALPHA),
            Color.Transparent
        )
    )
    
    Surface(
        modifier = modifier
            .border(
                width = GlassConfig.DEFAULT_BORDER_WIDTH,
                color = outlineColor.copy(alpha = GlassConfig.BORDER_ALPHA),
                shape = shape
            ),
        shape = shape,
        color = glassColor,
        tonalElevation = tonalElevation
    ) {
        Box {
            // Overlay for simulated blur/light effect
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayBrush)
            )
            content()
        }
    }
}

// =============================================================================
// Glass Color Utilities
// =============================================================================

/**
 * Returns the appropriate glass background color based on theme.
 */
@Composable
fun glassBackgroundColor(): Color {
    val isDarkTheme = isSystemInDarkTheme()
    val alpha = if (isDarkTheme) GlassConfig.DARK_MODE_ALPHA else GlassConfig.LIGHT_MODE_ALPHA
    return MaterialTheme.colorScheme.surface.copy(alpha = alpha)
}

/**
 * Returns the appropriate glass border color.
 */
@Composable
fun glassBorderColor(): Color {
    return MaterialTheme.colorScheme.outline.copy(alpha = GlassConfig.BORDER_ALPHA)
}
