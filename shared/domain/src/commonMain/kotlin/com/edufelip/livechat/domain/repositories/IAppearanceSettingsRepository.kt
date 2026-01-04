package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

interface IAppearanceSettingsRepository {
    fun observeSettings(): Flow<AppearanceSettings>

    suspend fun updateThemeMode(mode: ThemeMode)

    suspend fun updateTextScale(scale: Float)

    suspend fun updateReduceMotion(enabled: Boolean)

    suspend fun updateHighContrast(enabled: Boolean)

    suspend fun resetSettings()
}
