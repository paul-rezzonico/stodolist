package fr.unilim.stodolist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A generic permission dialog composable using Material3 AlertDialog.
 * 
 * This dialog is used to display permission-related messages to the user,
 * such as rationale explanations or settings redirect prompts.
 *
 * @param title The dialog title
 * @param message The message explaining why the permission is needed
 * @param confirmText Text for the confirm button
 * @param dismissText Text for the dismiss button
 * @param onConfirm Callback when the confirm button is clicked
 * @param onDismiss Callback when the dialog is dismissed or dismiss button is clicked
 * @param icon Optional icon to display in the dialog
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun PermissionDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * A convenience composable for showing a permission rationale dialog.
 * 
 * @param permissionName The name of the permission being requested
 * @param rationale The explanation of why the permission is needed
 * @param onRequestPermission Callback to request the permission
 * @param onDismiss Callback when the dialog is dismissed
 * @param icon Optional icon to display
 */
@Composable
fun PermissionRationaleDialog(
    permissionName: String,
    rationale: String,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null
) {
    PermissionDialog(
        title = "$permissionName Required",
        message = rationale,
        confirmText = "Grant Permission",
        dismissText = "Not Now",
        onConfirm = onRequestPermission,
        onDismiss = onDismiss,
        icon = icon
    )
}

/**
 * A convenience composable for showing a settings redirect dialog
 * when a permission has been permanently denied.
 * 
 * @param permissionName The name of the permission that was denied
 * @param message The explanation of why the permission is needed and how to enable it
 * @param onOpenSettings Callback to open app settings
 * @param onDismiss Callback when the dialog is dismissed
 * @param icon Optional icon to display
 */
@Composable
fun PermissionSettingsDialog(
    permissionName: String,
    message: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null
) {
    PermissionDialog(
        title = "$permissionName Disabled",
        message = message,
        confirmText = "Open Settings",
        dismissText = "Cancel",
        onConfirm = onOpenSettings,
        onDismiss = onDismiss,
        icon = icon
    )
}
