package com.edufelip.livechat.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val settings: NotificationSettings = NotificationSettings(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
