package com.edufelip.livechat.domain.models

data class AppearanceSettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppearanceSettings = AppearanceSettings(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
