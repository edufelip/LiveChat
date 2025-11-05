package com.project.livechat.composeapp.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidColorSchemeStrategy(
    private val fallback: PlatformColorSchemeStrategy,
) : PlatformColorSchemeStrategy {
    @Composable
    override fun scheme(
        isDarkTheme: Boolean,
        palette: LiveChatPalette,
    ): ColorScheme {
        val context = LocalContext.current
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("NewApi")
            val dynamicScheme =
                if (isDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            dynamicScheme
        } else {
            fallback.scheme(isDarkTheme, palette)
        }
    }
}

@Composable
actual fun rememberPlatformColorSchemeStrategy(): PlatformColorSchemeStrategy {
    val fallback = remember { PastelColorSchemeStrategy() }
    return remember { AndroidColorSchemeStrategy(fallback) }
}
