package com.edufelip.livechat.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidColorSchemeStrategy(
    private val context: Context,
    private val fallback: PlatformColorSchemeStrategy,
) : PlatformColorSchemeStrategy {
    override fun scheme(
        isDarkTheme: Boolean,
        palette: LiveChatPalette,
    ): ColorScheme {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            fallback.scheme(isDarkTheme, palette)
        }
    }
}

@Composable
actual fun rememberPlatformColorSchemeStrategy(): PlatformColorSchemeStrategy {
    val context = LocalContext.current.applicationContext
    val fallback = PastelColorSchemeStrategy()
    return remember(context, fallback) { AndroidColorSchemeStrategy(context, fallback) }
}
