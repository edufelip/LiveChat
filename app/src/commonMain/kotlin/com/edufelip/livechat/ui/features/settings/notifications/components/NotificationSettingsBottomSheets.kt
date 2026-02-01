package com.edufelip.livechat.ui.features.settings.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.components.atoms.BottomSheetDragHandle
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationQuietHoursBottomSheet(
    title: String,
    description: String,
    fromLabel: String,
    toLabel: String,
    fromPlaceholder: String,
    toPlaceholder: String,
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
        dragHandle = { BottomSheetDragHandle() },
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
                placeholder = { Text(fromPlaceholder) },
                singleLine = true,
                enabled = !isUpdating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = toValue,
                onValueChange = { onToChange(sanitizeTimeInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(toLabel) },
                placeholder = { Text(toPlaceholder) },
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
private fun NotificationQuietHoursBottomSheetPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        var from by remember { mutableStateOf(strings.notifications.quietHoursFromPlaceholder) }
        var to by remember { mutableStateOf(strings.notifications.quietHoursToPlaceholder) }
        NotificationQuietHoursBottomSheet(
            title = strings.notifications.quietHoursSheetTitle,
            description = strings.notifications.quietHoursSheetDescription,
            fromLabel = strings.notifications.quietHoursFromLabel,
            toLabel = strings.notifications.quietHoursToLabel,
            fromPlaceholder = strings.notifications.quietHoursFromPlaceholder,
            toPlaceholder = strings.notifications.quietHoursToPlaceholder,
            fromValue = from,
            toValue = to,
            onFromChange = { from = it },
            onToChange = { to = it },
            onDismiss = {},
            onConfirm = {},
            confirmEnabled = true,
            confirmLabel = strings.notifications.saveCta,
            isUpdating = false,
        )
    }
}
