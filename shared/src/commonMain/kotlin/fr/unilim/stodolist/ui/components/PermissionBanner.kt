package fr.unilim.stodolist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp

/**
 * Warning colors for the permission banner.
 * Uses amber/orange tones to convey warning state.
 */
private val WarningContainerColor = Color(0xFFFFF3E0) // Light orange
private val WarningContentColor = Color(0xFFE65100) // Dark orange
private val WarningContainerColorDark = Color(0xFF3E2723) // Dark brown for dark theme
private val WarningContentColorDark = Color(0xFFFFCC80) // Light orange for dark theme

/**
 * A banner/card that shows when permission is denied.
 * 
 * Displays a warning message explaining why the permission is needed,
 * with buttons to grant permission or dismiss the banner.
 *
 * @param message The message explaining why the permission is needed
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when "Maybe Later" or close button is clicked
 * @param modifier Optional modifier for the banner
 * @param visible Whether the banner is visible (for animation)
 * @param icon Optional icon to display (defaults to warning icon)
 * @param grantButtonText Text for the grant permission button
 * @param dismissButtonText Text for the dismiss button
 * @param useDarkTheme Whether to use dark theme colors
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
    useDarkTheme: Boolean = false
) {
    val containerColor = if (useDarkTheme) WarningContainerColorDark else WarningContainerColor
    val contentColor = if (useDarkTheme) WarningContentColorDark else WarningContentColor

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Warning",
                        modifier = Modifier.size(24.dp),
                        tint = contentColor
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.width(8.dp))

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

/**
 * A simplified notification permission banner with predefined text.
 *
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when the banner is dismissed
 * @param visible Whether the banner is visible
 * @param modifier Optional modifier
 * @param useDarkTheme Whether to use dark theme colors
 */
@Composable
fun NotificationPermissionBanner(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean = false
) {
    PermissionBanner(
        message = "Notifications are disabled. Enable them to receive task reminders and never miss a deadline.",
        onGrantPermission = onGrantPermission,
        onDismiss = onDismiss,
        modifier = modifier,
        visible = visible,
        grantButtonText = "Enable Notifications",
        dismissButtonText = "Maybe Later",
        useDarkTheme = useDarkTheme
    )
}

/**
 * A simplified alarm permission banner with predefined text.
 *
 * @param onGrantPermission Callback when "Grant Permission" button is clicked
 * @param onDismiss Callback when the banner is dismissed
 * @param visible Whether the banner is visible
 * @param modifier Optional modifier
 * @param useDarkTheme Whether to use dark theme colors
 */
@Composable
fun AlarmPermissionBanner(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean = false
) {
    PermissionBanner(
        message = "Exact alarm permission is required for precise task reminders. Without it, reminders may be delayed.",
        onGrantPermission = onGrantPermission,
        onDismiss = onDismiss,
        modifier = modifier,
        visible = visible,
        grantButtonText = "Enable Alarms",
        dismissButtonText = "Maybe Later",
        useDarkTheme = useDarkTheme
    )
}
