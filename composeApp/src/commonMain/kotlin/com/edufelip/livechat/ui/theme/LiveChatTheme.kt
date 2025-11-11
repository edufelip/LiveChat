package com.edufelip.livechat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.LocalLiveChatStrings
import com.edufelip.livechat.ui.resources.rememberLiveChatStrings

@Composable
fun LiveChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: LiveChatPalette = LiveChatPalettes.Pastel,
    strategy: PlatformColorSchemeStrategy = rememberPlatformColorSchemeStrategy(),
    strings: LiveChatStrings? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme = strategy.scheme(darkTheme, palette)
    val resolvedStrings = strings ?: rememberLiveChatStrings()

    CompositionLocalProvider(
        LocalLiveChatSpacing provides LiveChatSpacing(),
        LocalLiveChatStrings provides resolvedStrings,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LiveChatTypography,
            shapes = LiveChatShapes,
            content = content,
        )
    }
}
