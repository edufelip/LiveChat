package com.edufelip.livechat.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class PrivacySettingsUiState(
    val isLoading: Boolean = true,
    val settings: PrivacySettings = PrivacySettings(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
