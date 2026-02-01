package com.edufelip.livechat.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LiveChatPreviewContainer(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    LiveChatPreviewTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun LiveChatPreviewContainerPreview() {
    LiveChatPreviewContainer {
        Text("Preview container")
    }
}

@Preview(name = "Preview container (dark)", showBackground = true)
@Composable
private fun LiveChatPreviewContainerDarkPreview() {
    LiveChatPreviewContainer(darkTheme = true) {
        Text("Preview container")
    }
}
