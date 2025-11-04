package com.project.livechat.composeapp.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable loading placeholder with consistent spacing and typography.
 */
@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun LoadingStatePreview() {
    LiveChatPreviewContainer {
        LoadingState(message = "Loading chats…")
    }
}
