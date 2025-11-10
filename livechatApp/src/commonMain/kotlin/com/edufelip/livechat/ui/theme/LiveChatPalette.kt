package com.edufelip.livechat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class LiveChatPalette(
    val light: ColorScheme,
    val dark: ColorScheme,
)

private fun ColorScheme.withTonalContainers(
    lowest: Color,
    low: Color,
    container: Color,
    high: Color,
    highest: Color,
): ColorScheme =
    copy(
        surfaceContainerLowest = lowest,
        surfaceContainerLow = low,
        surfaceContainer = container,
        surfaceContainerHigh = high,
        surfaceContainerHighest = highest,
    )

object LiveChatPalettes {
    val Pastel =
        LiveChatPalette(
            light =
                lightColorScheme(
                    primary = Color(0xFF006A60),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFF9EF2E4),
                    onPrimaryContainer = Color(0xFF005048),
                    secondary = Color(0xFF4A635F),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFCCE8E2),
                    onSecondaryContainer = Color(0xFF334B47),
                    tertiary = Color(0xFF456179),
                    onTertiary = Color(0xFFFFFFFF),
                    tertiaryContainer = Color(0xFFCCE5FF),
                    onTertiaryContainer = Color(0xFF2D4961),
                    error = Color(0xFFBA1A1A),
                    onError = Color(0xFFFFFFFF),
                    errorContainer = Color(0xFFFFDAD6),
                    onErrorContainer = Color(0xFF93000A),
                    background = Color(0xFFF4FBF8),
                    onBackground = Color(0xFF161D1C),
                    surface = Color(0xFFF4FBF8),
                    onSurface = Color(0xFF161D1C),
                    surfaceVariant = Color(0xFFDAE5E1),
                    onSurfaceVariant = Color(0xFF3F4947),
                    outline = Color(0xFF6F7977),
                    inverseSurface = Color(0xFF2B3230),
                    inverseOnSurface = Color(0xFFECF2EF),
                    inversePrimary = Color(0xFF82D5C8),
                    surfaceTint = Color(0xFF006A60),
                ).withTonalContainers(
                    lowest = Color(0xFFFFFFFF),
                    low = Color(0xFFEFF5F2),
                    container = Color(0xFFE9EFED),
                    high = Color(0xFFE3EAE7),
                    highest = Color(0xFFDDE4E1),
                ),
            dark =
                darkColorScheme(
                    primary = Color(0xFF82D5C8),
                    onPrimary = Color(0xFF003731),
                    primaryContainer = Color(0xFF005048),
                    onPrimaryContainer = Color(0xFF9EF2E4),
                    secondary = Color(0xFFB1CCC6),
                    onSecondary = Color(0xFF1C3531),
                    secondaryContainer = Color(0xFF334B47),
                    onSecondaryContainer = Color(0xFFCCE8E2),
                    tertiary = Color(0xFFADCAE6),
                    onTertiary = Color(0xFF153349),
                    tertiaryContainer = Color(0xFF2D4961),
                    onTertiaryContainer = Color(0xFFCCE5FF),
                    error = Color(0xFFFFB4AB),
                    onError = Color(0xFF690005),
                    errorContainer = Color(0xFF93000A),
                    onErrorContainer = Color(0xFFFFDAD6),
                    background = Color(0xFF0E1513),
                    onBackground = Color(0xFFDDE4E1),
                    surface = Color(0xFF0E1513),
                    onSurface = Color(0xFFDDE4E1),
                    surfaceVariant = Color(0xFF3F4947),
                    onSurfaceVariant = Color(0xFFBEC9C6),
                    outline = Color(0xFF899390),
                    inverseSurface = Color(0xFFDDE4E1),
                    inverseOnSurface = Color(0xFF2B3230),
                    inversePrimary = Color(0xFF006A60),
                    surfaceTint = Color(0xFF82D5C8),
                ).withTonalContainers(
                    lowest = Color(0xFF090F0E),
                    low = Color(0xFF161D1C),
                    container = Color(0xFF1A2120),
                    high = Color(0xFF252B2A),
                    highest = Color(0xFF303635),
                ),
        )
}

class PastelColorSchemeStrategy : PlatformColorSchemeStrategy {
    override fun scheme(
        isDarkTheme: Boolean,
        palette: LiveChatPalette,
    ): ColorScheme = if (isDarkTheme) palette.dark else palette.light
}
