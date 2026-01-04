package com.edufelip.livechat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.LocalLiveChatStrings
import com.edufelip.livechat.ui.resources.rememberLiveChatStrings

@Composable
fun LiveChatTheme(
    themeMode: ThemeMode = ThemeMode.System,
    textScale: Float = 1.0f,
    reduceMotion: Boolean = false,
    highContrast: Boolean = false,
    palette: LiveChatPalette = LiveChatPalettes.Pastel,
    strategy: PlatformColorSchemeStrategy = rememberPlatformColorSchemeStrategy(),
    strings: LiveChatStrings? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    val baseScheme = strategy.scheme(darkTheme, palette)
    val colorScheme = if (highContrast) baseScheme.highContrast(darkTheme) else baseScheme
    val typography = LiveChatTypography.scaled(textScale)
    val resolvedStrings = strings ?: rememberLiveChatStrings()

    CompositionLocalProvider(
        LocalLiveChatSpacing provides LiveChatSpacing(),
        LocalLiveChatStrings provides resolvedStrings,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = LiveChatShapes,
            content = content,
        )
    }
}
