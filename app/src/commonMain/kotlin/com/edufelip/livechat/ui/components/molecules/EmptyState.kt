package com.edufelip.livechat.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Generic empty-state text used when no items are available.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    tone: Color? = null,
) {
    val resolvedTone = tone ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = resolvedTone,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun EmptyStatePreview() {
    LiveChatPreviewContainer {
        EmptyState(message = liveChatStrings().conversation.emptyList)
    }
}
