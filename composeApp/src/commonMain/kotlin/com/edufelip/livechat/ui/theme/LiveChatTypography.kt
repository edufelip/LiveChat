package com.edufelip.livechat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

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
        bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.SansSerif, lineHeight = 28.sp),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.SansSerif, lineHeight = 24.sp),
        bodySmall = Typography().bodySmall.copy(fontFamily = FontFamily.SansSerif, lineHeight = 20.sp),
        labelLarge = Typography().labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        labelMedium = Typography().labelMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        labelSmall = Typography().labelSmall.copy(fontFamily = FontFamily.SansSerif),
    )

fun Typography.scaled(scale: Float): Typography {
    val clamped = scale.coerceIn(0.9f, 1.2f)
    return Typography(
        displayLarge = displayLarge.scale(clamped),
        displayMedium = displayMedium.scale(clamped),
        displaySmall = displaySmall.scale(clamped),
        headlineLarge = headlineLarge.scale(clamped),
        headlineMedium = headlineMedium.scale(clamped),
        headlineSmall = headlineSmall.scale(clamped),
        titleLarge = titleLarge.scale(clamped),
        titleMedium = titleMedium.scale(clamped),
        titleSmall = titleSmall.scale(clamped),
        bodyLarge = bodyLarge.scale(clamped),
        bodyMedium = bodyMedium.scale(clamped),
        bodySmall = bodySmall.scale(clamped),
        labelLarge = labelLarge.scale(clamped),
        labelMedium = labelMedium.scale(clamped),
        labelSmall = labelSmall.scale(clamped),
    )
}

private fun TextStyle.scale(scale: Float): TextStyle =
    copy(
        fontSize = fontSize.scaleTextUnit(scale),
        lineHeight = lineHeight.scaleTextUnit(scale),
    )

private fun TextUnit.scaleTextUnit(scale: Float): TextUnit =
    if (isUnspecified) {
        this
    } else {
        (value * scale).sp
    }
