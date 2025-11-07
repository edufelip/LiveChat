package com.project.livechat.composeapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

interface PlatformColorSchemeStrategy {
    fun scheme(
        isDarkTheme: Boolean,
        palette: LiveChatPalette,
    ): ColorScheme
}

@Composable
expect fun rememberPlatformColorSchemeStrategy(): PlatformColorSchemeStrategy
