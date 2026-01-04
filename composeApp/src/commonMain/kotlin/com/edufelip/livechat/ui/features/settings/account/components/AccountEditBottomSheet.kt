package com.edufelip.livechat.ui.features.settings.account.components

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
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountEditBottomSheet(
    title: String,
    description: String,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    isUpdating: Boolean,
    confirmLabel: String,
    keyboardOptions: KeyboardOptions,
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
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                enabled = !isUpdating,
                keyboardOptions = keyboardOptions,
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

@DevicePreviews
@Preview
@Composable
private fun AccountEditBottomSheetPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        var text by remember { mutableStateOf(strings.account.displayNameMissing) }
        AccountEditBottomSheet(
            title = strings.account.editDisplayNameTitle,
            description = strings.account.editDisplayNameDescription,
            label = strings.account.displayNameLabel,
            placeholder = strings.account.displayNameMissing,
            value = text,
            onValueChange = { text = it },
            onDismiss = {},
            onConfirm = {},
            confirmEnabled = true,
            isUpdating = false,
            confirmLabel = strings.account.saveCta,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
    }
}
