package com.project.livechat.composeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val LiveChatTypography =
    Typography(
        displayLarge = Typography().displayLarge.copy(fontFamily = FontFamily.SansSerif),
        displayMedium = Typography().displayMedium.copy(fontFamily = FontFamily.SansSerif),
        displaySmall = Typography().displaySmall.copy(fontFamily = FontFamily.SansSerif),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleMedium = Typography().titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        titleSmall = Typography().titleSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        bodySmall = Typography().bodySmall.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = Typography().labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        labelMedium = Typography().labelMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        labelSmall = Typography().labelSmall.copy(fontFamily = FontFamily.SansSerif),
    )
