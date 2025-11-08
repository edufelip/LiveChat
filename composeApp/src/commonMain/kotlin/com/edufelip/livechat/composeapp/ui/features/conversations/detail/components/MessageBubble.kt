package com.edufelip.livechat.composeapp.ui.features.conversations.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.composeapp.preview.DevicePreviews
import com.edufelip.livechat.composeapp.preview.LiveChatPreviewContainer
import com.edufelip.livechat.composeapp.preview.PreviewFixtures
import com.edufelip.livechat.composeapp.ui.util.formatAsTime
import com.edufelip.livechat.domain.models.Message
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Single chat bubble aligned by sender ownership.
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    modifier: Modifier = Modifier,
) {
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bubbleColor =
        if (isOwn) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    val textColor =
        if (isOwn) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Surface(color = bubbleColor, shape = RoundedCornerShape(16.dp)) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                text = message.body,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.createdAt.formatAsTime(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun MessageBubblePreview() {
    LiveChatPreviewContainer {
        val message = PreviewFixtures.conversationUiState.messages.first()
        MessageBubble(message = message, isOwn = message.senderId == "user")
    }
}
