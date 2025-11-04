package com.project.livechat.composeapp.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Generic empty-state text used when no items are available.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    tone: Color = Color.Gray,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = tone,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun EmptyStatePreview() {
    LiveChatPreviewContainer {
        EmptyState(message = "No conversations yet")
    }
}
