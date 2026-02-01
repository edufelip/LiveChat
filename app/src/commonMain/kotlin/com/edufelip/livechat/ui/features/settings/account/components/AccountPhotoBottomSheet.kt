package com.edufelip.livechat.ui.features.settings.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.components.atoms.BottomSheetDragHandle
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountPhotoBottomSheet(
    title: String,
    description: String,
    pickLabel: String,
    takeLabel: String,
    onPick: () -> Unit,
    onTake: () -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean,
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Button(
                onClick = onPick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
            ) {
                Text(pickLabel)
            }
            OutlinedButton(
                onClick = onTake,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
            ) {
                Text(takeLabel)
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun AccountPhotoBottomSheetPreview() {
    val strings = liveChatStrings()
    LiveChatPreviewContainer {
        AccountPhotoBottomSheet(
            title = strings.account.photoSheetTitle,
            description = strings.account.photoSheetDescription,
            pickLabel = strings.conversation.pickImage,
            takeLabel = strings.conversation.takePhoto,
            onPick = {},
            onTake = {},
            onDismiss = {},
            isProcessing = false,
        )
    }
}
