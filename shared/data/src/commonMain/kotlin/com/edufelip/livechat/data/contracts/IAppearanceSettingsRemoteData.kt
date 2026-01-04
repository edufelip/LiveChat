package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode

interface IAppearanceSettingsRemoteData {
    suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): AppearanceSettings?

    suspend fun updateThemeMode(
        userId: String,
        idToken: String,
        mode: ThemeMode,
    )

    suspend fun updateTextScale(
        userId: String,
        idToken: String,
        scale: Float,
    )

    suspend fun updateReduceMotion(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )

    suspend fun updateHighContrast(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )

    suspend fun resetSettings(
        userId: String,
        idToken: String,
    )
}
