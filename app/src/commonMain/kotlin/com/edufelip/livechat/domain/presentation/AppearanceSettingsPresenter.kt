package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.AppearanceSettingsUiState
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.domain.useCases.ObserveAppearanceSettingsUseCase
import com.edufelip.livechat.domain.useCases.ResetAppearanceSettingsUseCase
import com.edufelip.livechat.domain.useCases.UpdateTextScaleUseCase
import com.edufelip.livechat.domain.useCases.UpdateThemeModeUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppearanceSettingsPresenter(
    private val observeSettings: ObserveAppearanceSettingsUseCase,
    private val updateThemeMode: UpdateThemeModeUseCase,
    private val updateTextScale: UpdateTextScaleUseCase,
    private val resetSettings: ResetAppearanceSettingsUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(AppearanceSettingsUiState())
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<AppearanceSettingsUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeSettings()
                .catch { throwable ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load appearance settings",
                        )
                    }
                }
                .collectLatest { settings ->
                    mutableState.update {
                        it.copy(isLoading = false, settings = settings, errorMessage = null)
                    }
                }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        updateSettings(
            update = { updateThemeMode(mode) },
            localUpdate = { it.copy(themeMode = mode) },
        )
    }

    fun updateTextScale(scale: Float) {
        updateSettings(
            update = { updateTextScale(scale) },
            localUpdate = { it.copy(textScale = scale) },
        )
    }

    fun resetAppearanceSettings() {
        updateSettings(
            update = { resetSettings() },
            localUpdate = { AppearanceSettings() },
        )
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun updateSettings(
        update: suspend () -> Unit,
        localUpdate: (AppearanceSettings) -> AppearanceSettings,
    ) {
        if (mutableState.value.isUpdating) return
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { update() }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            settings = localUpdate(state.settings),
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Update failed",
                        )
                    }
                }
        }
    }
}
