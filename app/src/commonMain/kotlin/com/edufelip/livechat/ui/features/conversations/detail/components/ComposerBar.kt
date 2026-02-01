package com.edufelip.livechat.ui.features.conversations.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.resources.liveChatStrings
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ComposerBar(
    isSending: Boolean,
    errorMessage: String? = null,
    onSend: (String) -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onPickImage: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onErrorClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val conversationStrings = liveChatStrings().conversation
    var text by rememberSaveable { mutableStateOf("") }
    val sendEnabled = text.isNotBlank() && !isSending

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                enabled = !isSending,
                onClick = onPickImage,
            ) {
                Icon(
                    imageVector = AppIcons.gallery,
                    contentDescription = conversationStrings.pickImage,
                )
            }
            FilledIconButton(
                enabled = !isSending,
                onClick = onTakePhoto,
            ) {
                Icon(
                    imageVector = AppIcons.camera,
                    contentDescription = conversationStrings.takePhoto,
                )
            }
            FilledIconButton(
                enabled = !isSending && !isRecording,
                onClick = onStartRecording,
            ) {
                val icon = if (isRecording) AppIcons.stop else AppIcons.mic
                val description =
                    if (isRecording) {
                        conversationStrings.stopRecording
                    } else {
                        conversationStrings.recordAudio
                    }
                Icon(imageVector = icon, contentDescription = description)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(conversationStrings.messagePlaceholder) },
                enabled = !isSending,
                maxLines = 4,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions =
                    KeyboardActions(
                        onSend = {
                            if (sendEnabled) {
                                val trimmed = text.trim()
                                onSend(trimmed)
                                text = ""
                            }
                        },
                    ),
            )
            if (!errorMessage.isNullOrBlank()) {
                FilledIconButton(
                    enabled = true,
                    onClick = { onErrorClick?.invoke() },
                ) {
                    Icon(
                        imageVector = AppIcons.error,
                        contentDescription = conversationStrings.messageFailed,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            FilledIconButton(
                enabled = sendEnabled,
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) {
                        onSend(trimmed)
                        text = ""
                    }
                },
            ) {
                Icon(
                    imageVector = AppIcons.confirm,
                    contentDescription = conversationStrings.sendMessage,
                )
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun ComposerBarPreview() {
    LiveChatPreviewContainer {
        ComposerBar(
            isSending = false,
            isRecording = false,
            onSend = {},
            onStartRecording = {},
            errorMessage = liveChatStrings().conversation.messageFailed,
        )
    }
}
