package com.edufelip.livechat.composeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.edufelip.livechat.composeapp.ui.resources.LiveChatStrings
import com.edufelip.livechat.composeapp.ui.resources.LocalLiveChatStrings

@Composable
fun LiveChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: LiveChatPalette = LiveChatPalettes.Pastel,
    strategy: PlatformColorSchemeStrategy = rememberPlatformColorSchemeStrategy(),
    strings: LiveChatStrings = LiveChatStrings(),
    content: @Composable () -> Unit,
) {
    val fallbackStrategy = remember { PastelColorSchemeStrategy() }
    val baseColors = fallbackStrategy.scheme(darkTheme, palette)
    val colorScheme =
        strategy.scheme(darkTheme, palette).copy(surfaceTint = baseColors.surfaceTint)

    CompositionLocalProvider(
        LocalLiveChatSpacing provides LiveChatSpacing(),
        LocalLiveChatStrings provides strings,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LiveChatTypography,
            shapes = LiveChatShapes,
            content = content,
        )
    }
}
