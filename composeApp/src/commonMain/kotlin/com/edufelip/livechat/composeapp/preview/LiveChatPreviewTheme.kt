package com.edufelip.livechat.composeapp.preview

import androidx.compose.runtime.Composable
import com.edufelip.livechat.composeapp.ui.theme.LiveChatTheme

@Composable
fun LiveChatPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    LiveChatTheme(darkTheme = darkTheme, content = content)
}
