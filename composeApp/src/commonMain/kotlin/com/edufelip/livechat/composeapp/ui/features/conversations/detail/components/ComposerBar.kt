package com.edufelip.livechat.composeapp.ui.features.conversations.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.composeapp.preview.DevicePreviews
import com.edufelip.livechat.composeapp.preview.LiveChatPreviewContainer
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Message composer with send and refresh actions.
 */
@Composable
fun ComposerBar(
    isSending: Boolean,
    onSend: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                modifier =
                    Modifier
                        .weight(1f),
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Message…") },
                enabled = !isSending,
                maxLines = 4,
            )
            Button(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) {
                        onSend(trimmed)
                        text = ""
                    }
                },
                enabled = !isSending,
            ) {
                Text(if (isSending) "Sending…" else "Send")
            }
        }
        TextButton(
            onClick = { scope.launch { onRefresh() } },
        ) {
            Text("Refresh conversation")
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun ComposerBarPreview() {
    LiveChatPreviewContainer {
        ComposerBar(isSending = false, onSend = {}, onRefresh = {})
    }
}
