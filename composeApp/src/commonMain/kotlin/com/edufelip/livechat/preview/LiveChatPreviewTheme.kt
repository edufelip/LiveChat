package com.edufelip.livechat.preview

import androidx.compose.runtime.Composable
import com.edufelip.livechat.ui.theme.LiveChatTheme

@Composable
fun LiveChatPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    LiveChatTheme(darkTheme = darkTheme, content = content)
}
