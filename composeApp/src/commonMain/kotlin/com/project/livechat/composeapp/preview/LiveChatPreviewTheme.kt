package com.project.livechat.composeapp.preview

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme: ColorScheme =
    lightColorScheme(
        primary = Color(0xFFB2DFDB),
        onPrimary = Color(0xFF1A2A2A),
        primaryContainer = Color(0xFFDDF4F1),
        onPrimaryContainer = Color(0xFF1A2A2A),
        secondary = Color(0xFFCFE8E6),
        onSecondary = Color(0xFF1A2A2A),
        secondaryContainer = Color(0xFFE8F5F4),
        onSecondaryContainer = Color(0xFF1A2A2A),
        tertiary = Color(0xFF80CBC4),
        onTertiary = Color(0xFF0F1F1D),
        tertiaryContainer = Color(0xFF2A403F),
        onTertiaryContainer = Color(0xFFD1E0DD),
        error = Color(0xFFBA1A1A),
        errorContainer = Color(0xFFFFDAD6),
        onError = Color(0xFFFFFFFF),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF0FDFA),
        onBackground = Color(0xFF3F5A57),
        surface = Color(0xFFF0FDFA),
        onSurface = Color(0xFF3F5A57),
        surfaceVariant = Color(0xFFE3F2F1),
        onSurfaceVariant = Color(0xFF4F6A67),
        outline = Color(0xFF91ACA7),
        inverseSurface = Color(0xFF3F5A57),
        inverseOnSurface = Color(0xFFE4F3F1),
        inversePrimary = Color(0xFF80CBC4),
        surfaceTint = Color(0xFFB2DFDB),
    )

private val DarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = Color(0xFF80CBC4),
        onPrimary = Color(0xFF0B1917),
        primaryContainer = Color(0xFF2A403F),
        onPrimaryContainer = Color(0xFFD1E0DD),
        secondary = Color(0xFF6A8D8A),
        onSecondary = Color(0xFFE0EEEC),
        secondaryContainer = Color(0xFF243534),
        onSecondaryContainer = Color(0xFFD1E0DD),
        tertiary = Color(0xFFB2DFDB),
        onTertiary = Color(0xFF0B1917),
        tertiaryContainer = Color(0xFF1F2E2D),
        onTertiaryContainer = Color(0xFFDDECE9),
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onError = Color(0xFF690005),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A2A2A),
        onBackground = Color(0xFFD1E0DD),
        surface = Color(0xFF1A2A2A),
        onSurface = Color(0xFFD1E0DD),
        surfaceVariant = Color(0xFF2F4341),
        onSurfaceVariant = Color(0xFFB0C4C1),
        outline = Color(0xFF4F6562),
        inverseOnSurface = Color(0xFF1A2A2A),
        inverseSurface = Color(0xFFE4F3F1),
        inversePrimary = Color(0xFFB2DFDB),
        surfaceTint = Color(0xFF80CBC4),
    )

@Composable
fun LiveChatPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content,
    )
}
