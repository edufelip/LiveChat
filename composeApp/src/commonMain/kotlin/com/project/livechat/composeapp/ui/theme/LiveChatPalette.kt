package com.project.livechat.composeapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class LiveChatPalette(
    val light: ColorScheme,
    val dark: ColorScheme,
)

object LiveChatPalettes {
    val Pastel =
        LiveChatPalette(
            light =
                lightColorScheme(
                    primary = Color(0xFF82D4B8),
                    onPrimary = Color(0xFF043428),
                    primaryContainer = Color(0xFFD4F5E7),
                    onPrimaryContainer = Color(0xFF041E16),
                    secondary = Color(0xFF6BC3B0),
                    onSecondary = Color(0xFF02332A),
                    secondaryContainer = Color(0xFFBCEDE1),
                    onSecondaryContainer = Color(0xFF05201B),
                    tertiary = Color(0xFF7FDBCE),
                    onTertiary = Color(0xFF00201B),
                    tertiaryContainer = Color(0xFFBBF0E6),
                    onTertiaryContainer = Color(0xFF00332B),
                    error = Color(0xFFBA1A1A),
                    onError = Color(0xFFFFFFFF),
                    errorContainer = Color(0xFFFFDAD6),
                    onErrorContainer = Color(0xFF410002),
                    background = Color(0xFFF5FBF8),
                    onBackground = Color(0xFF1B2F2A),
                    surface = Color(0xFFF5FBF8),
                    onSurface = Color(0xFF1B2F2A),
                    surfaceVariant = Color(0xFFD9E6E1),
                    onSurfaceVariant = Color(0xFF3E5550),
                    outline = Color(0xFF6A807B),
                    inverseSurface = Color(0xFF223833),
                    inverseOnSurface = Color(0xFFE0F0EA),
                    inversePrimary = Color(0xFF64C7A8),
                    surfaceTint = Color(0xFF82D4B8),
                ),
            dark =
                darkColorScheme(
                    primary = Color(0xFF64C7A8),
                    onPrimary = Color(0xFF003528),
                    primaryContainer = Color(0xFF0F513B),
                    onPrimaryContainer = Color(0xFFC0F2DC),
                    secondary = Color(0xFF58B6A4),
                    onSecondary = Color(0xFF00332B),
                    secondaryContainer = Color(0xFF0D4F41),
                    onSecondaryContainer = Color(0xFFBBEFE3),
                    tertiary = Color(0xFF68CDC1),
                    onTertiary = Color(0xFF00332C),
                    tertiaryContainer = Color(0xFF005046),
                    onTertiaryContainer = Color(0xFFB3EDE3),
                    error = Color(0xFFFFB4AB),
                    onError = Color(0xFF690005),
                    errorContainer = Color(0xFF93000A),
                    onErrorContainer = Color(0xFFFFDAD6),
                    background = Color(0xFF0F1B18),
                    onBackground = Color(0xFFD2E5DE),
                    surface = Color(0xFF0F1B18),
                    onSurface = Color(0xFFD2E5DE),
                    surfaceVariant = Color(0xFF30443F),
                    onSurfaceVariant = Color(0xFFB5CAC4),
                    outline = Color(0xFF80948F),
                    inverseSurface = Color(0xFFD2E5DE),
                    inverseOnSurface = Color(0xFF142421),
                    inversePrimary = Color(0xFF82D4B8),
                    surfaceTint = Color(0xFF64C7A8),
                ),
        )
}

class PastelColorSchemeStrategy : PlatformColorSchemeStrategy {
    override fun scheme(
        isDarkTheme: Boolean,
        palette: LiveChatPalette,
    ): ColorScheme = if (isDarkTheme) palette.dark else palette.light
}
