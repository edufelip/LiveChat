package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.domain.repositories.IAppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppearanceSettingsUseCase(
    private val repository: IAppearanceSettingsRepository,
) {
    operator fun invoke(): Flow<AppearanceSettings> = repository.observeSettings()
}

class UpdateThemeModeUseCase(
    private val repository: IAppearanceSettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) {
        repository.updateThemeMode(mode)
    }
}

class UpdateTextScaleUseCase(
    private val repository: IAppearanceSettingsRepository,
) {
    suspend operator fun invoke(scale: Float) {
        repository.updateTextScale(scale)
    }
}

class ResetAppearanceSettingsUseCase(
    private val repository: IAppearanceSettingsRepository,
) {
    suspend operator fun invoke() {
        repository.resetSettings()
    }
}
