package com.edufelip.livechat.domain.models

enum class ThemeMode {
    System,
    Light,
    Dark,
}

data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val textScale: Float = DEFAULT_TEXT_SCALE,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
) {
    companion object {
        const val DEFAULT_TEXT_SCALE = 1.0f
        const val MIN_TEXT_SCALE = 0.9f
        const val MAX_TEXT_SCALE = 1.2f
    }
}
