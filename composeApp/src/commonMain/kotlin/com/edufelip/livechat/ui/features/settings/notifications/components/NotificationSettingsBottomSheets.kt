package com.edufelip.livechat.ui.features.settings.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

internal data class NotificationSoundOption(
    val id: String,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationSoundBottomSheet(
    title: String,
    description: String,
    options: List<NotificationSoundOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    confirmLabel: String,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { ModalBottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(bottom = MaterialTheme.spacing.lg)
                    .navigationBarsPadding()
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                options.forEach { option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedId == option.id,
                                    role = Role.RadioButton,
                                    onClick = { onSelect(option.id) },
                                )
                                .padding(vertical = MaterialTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        RadioButton(
                            selected = selectedId == option.id,
                            onClick = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = confirmEnabled,
            ) {
                Text(confirmLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationQuietHoursBottomSheet(
    title: String,
    description: String,
    fromLabel: String,
    toLabel: String,
    fromValue: String,
    toValue: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    confirmLabel: String,
    isUpdating: Boolean,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { ModalBottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(bottom = MaterialTheme.spacing.lg)
                    .navigationBarsPadding()
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = fromValue,
                onValueChange = { onFromChange(sanitizeTimeInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(fromLabel) },
                placeholder = { Text("22:00") },
                singleLine = true,
                enabled = !isUpdating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = toValue,
                onValueChange = { onToChange(sanitizeTimeInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(toLabel) },
                placeholder = { Text("07:00") },
                singleLine = true,
                enabled = !isUpdating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = confirmEnabled && !isUpdating,
            ) {
                Text(confirmLabel)
            }
        }
    }
}

private fun sanitizeTimeInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == ':' }
    return filtered.take(5)
}

@DevicePreviews
@Preview
@Composable
private fun NotificationSoundBottomSheetPreview() {
    LiveChatPreviewContainer {
        NotificationSoundBottomSheet(
            title = "Notification sound",
            description = "Choose a tone for incoming messages.",
            options =
                listOf(
                    NotificationSoundOption("Popcorn", "Popcorn"),
                    NotificationSoundOption("Chime", "Chime"),
                ),
            selectedId = "Popcorn",
            onSelect = {},
            onDismiss = {},
            onConfirm = {},
            confirmEnabled = true,
            confirmLabel = "Save",
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun NotificationQuietHoursBottomSheetPreview() {
    var from by remember { mutableStateOf("22:00") }
    var to by remember { mutableStateOf("07:00") }
    LiveChatPreviewContainer {
        NotificationQuietHoursBottomSheet(
            title = "Edit quiet hours",
            description = "Use 24h format (HH:MM).",
            fromLabel = "From",
            toLabel = "To",
            fromValue = from,
            toValue = to,
            onFromChange = { from = it },
            onToChange = { to = it },
            onDismiss = {},
            onConfirm = {},
            confirmEnabled = true,
            confirmLabel = "Save",
            isUpdating = false,
        )
    }
}
