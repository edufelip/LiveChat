package com.edufelip.livechat.ui.features.settings.appearance

import com.edufelip.livechat.domain.models.AppearanceSettings
import kotlin.math.roundToInt

internal fun clampTextScale(scale: Float): Float =
    scale.coerceIn(
        AppearanceSettings.MIN_TEXT_SCALE,
        AppearanceSettings.MAX_TEXT_SCALE,
    )

internal fun scaleFromSlider(value: Float): Float {
    val clamped = value.coerceIn(0f, 100f)
    val range = AppearanceSettings.MAX_TEXT_SCALE - AppearanceSettings.MIN_TEXT_SCALE
    return AppearanceSettings.MIN_TEXT_SCALE + (range * (clamped / 100f))
}

internal fun sliderFromScale(scale: Float): Float {
    val clamped = clampTextScale(scale)
    val range = AppearanceSettings.MAX_TEXT_SCALE - AppearanceSettings.MIN_TEXT_SCALE
    if (range == 0f) return 50f
    return ((clamped - AppearanceSettings.MIN_TEXT_SCALE) / range) * 100f
}

internal fun Float.roundToTwoDecimals(): Float = (this * 100f).roundToInt() / 100f
