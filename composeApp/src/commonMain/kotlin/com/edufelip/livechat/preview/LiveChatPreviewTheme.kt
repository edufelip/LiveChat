package com.edufelip.livechat.preview

import androidx.compose.runtime.Composable
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.ui.theme.LiveChatTheme

@Composable
fun LiveChatPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val mode = if (darkTheme) ThemeMode.Dark else ThemeMode.Light
    LiveChatTheme(themeMode = mode, content = content)
}
