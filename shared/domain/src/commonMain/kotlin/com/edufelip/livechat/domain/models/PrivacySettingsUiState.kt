package com.edufelip.livechat.domain.models

data class PrivacySettingsUiState(
    val isLoading: Boolean = true,
    val settings: PrivacySettings = PrivacySettings(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
