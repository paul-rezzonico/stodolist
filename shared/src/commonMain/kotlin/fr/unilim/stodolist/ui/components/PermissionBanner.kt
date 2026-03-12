package fr.unilim.stodolist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import fr.unilim.stodolist.ui.theme.AnimationDuration
import fr.unilim.stodolist.ui.theme.GlassCard
import fr.unilim.stodolist.ui.theme.IconSize
import fr.unilim.stodolist.ui.theme.Spacing

// =============================================================================
// Warning/Error Colors
// =============================================================================

/**
 * Warning colors matching the app's teal/cyan theme.
 * Uses amber/orange tones to convey warning state.
 */
private object WarningColors {
    // Light theme warning colors
    val containerLight = Color(0xFFFFF3E0) // Light orange
    val contentLight = Color(0xFFE65100)   // Dark orange

    // Dark theme warning colors
    val containerDark = Color(0xFF3E2723)  // Dark brown
    val contentDark = Color(0xFFFFCC80)    // Light orange
}

/**
 * Error colors matching the app's theme.
 */
private object ErrorColors {
    // Light theme error colors
    val containerLight = Color(0xFFFFEBEE) // Light red
    val contentLight = Color(0xFFC62828)   // Dark red

    // Dark theme error colors
    val containerDark = Color(0xFF3E2723)  // Dark brown-red
    val contentDark = Color(0xFFFF8A80)    // Light red (matches theme DarkError)
}

// =============================================================================
// Permission Banner Component
// =============================================================================

/**
 * A banner/card that shows when permission is denied.
 *
 * Displays a warning message explaining why the permission is needed,
 * with buttons to grant permission or dismiss the banner.
 * Uses GlassCard styling with smooth show/hide animations.
 *
 * @param message The message explaining why the permission is needed
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when "Maybe Later" or close button is clicked
 * @param modifier Optional modifier for the banner
 * @param visible Whether the banner is visible (for animation)
 * @param icon Optional icon to display (defaults to warning icon)
 * @param grantButtonText Text for the grant permission button
 * @param dismissButtonText Text for the dismiss button
 * @param isError Whether to use error styling instead of warning (default false)
 */
@Composable
fun PermissionBanner(
    message: String,
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    icon: ImageVector = Icons.Default.Warning,
    grantButtonText: String = "Grant Permission",
    dismissButtonText: String = "Maybe Later",
    isError: Boolean = false
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Select colors based on error/warning state and theme
    val contentColor = when {
        isError && isDarkTheme -> ErrorColors.contentDark
        isError -> ErrorColors.contentLight
        isDarkTheme -> WarningColors.contentDark
        else -> WarningColors.contentLight
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(AnimationDuration.medium)
        ) + fadeIn(
            animationSpec = tween(AnimationDuration.medium)
        ),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(AnimationDuration.medium)
        ) + fadeOut(
            animationSpec = tween(AnimationDuration.medium)
        )
    ) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isError) "Error" else "Warning",
                        modifier = Modifier.size(IconSize.medium),
                        tint = contentColor
                    )

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(IconSize.medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = contentColor
                        )
                    ) {
                        Text(
                            text = dismissButtonText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    Button(
                        onClick = onGrantPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = contentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = grantButtonText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Convenience Composables for Common Permission Banners
// =============================================================================

/**
 * A simplified notification permission banner with predefined text.
 *
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when the banner is dismissed
 * @param visible Whether the banner is visible
 * @param modifier Optional modifier
 */
@Composable
fun NotificationPermissionBanner(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    PermissionBanner(
        message = "Notifications are disabled. Enable them to receive task reminders and never miss a deadline.",
        onGrantPermission = onGrantPermission,
        onDismiss = onDismiss,
        modifier = modifier,
        visible = visible,
        grantButtonText = "Enable Notifications",
        dismissButtonText = "Maybe Later"
    )
}

/**
 * A simplified alarm permission banner with predefined text.
 *
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when the banner is dismissed
 * @param visible Whether the banner is visible
 * @param modifier Optional modifier
 */
@Composable
fun AlarmPermissionBanner(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    PermissionBanner(
        message = "Exact alarm permission is required for precise task reminders. Without it, reminders may be delayed.",
        onGrantPermission = onGrantPermission,
        onDismiss = onDismiss,
        modifier = modifier,
        visible = visible,
        grantButtonText = "Enable Alarms",
        dismissButtonText = "Maybe Later"
    )
}
