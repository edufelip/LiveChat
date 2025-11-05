package com.project.livechat.composeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun LiveChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: LiveChatPalette = LiveChatPalettes.Pastel,
    strategy: PlatformColorSchemeStrategy = rememberPlatformColorSchemeStrategy(),
    content: @Composable () -> Unit,
) {
    val fallbackStrategy = remember { PastelColorSchemeStrategy() }
    val baseColors = fallbackStrategy.scheme(darkTheme, palette)
    val colorScheme =
        strategy.scheme(darkTheme, palette).copy(surfaceTint = baseColors.surfaceTint)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LiveChatTypography,
        shapes = LiveChatShapes,
        content = content,
    )
}
