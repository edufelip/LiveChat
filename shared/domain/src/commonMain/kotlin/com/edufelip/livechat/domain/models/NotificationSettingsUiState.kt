package com.edufelip.livechat.domain.models

data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val settings: NotificationSettings = NotificationSettings(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
