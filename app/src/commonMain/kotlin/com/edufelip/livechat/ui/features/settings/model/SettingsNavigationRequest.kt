package com.edufelip.livechat.ui.features.settings.model

data class SettingsNavigationRequest(
    val section: SettingsSection,
    val title: String,
    val description: String,
    val placeholderMessage: String,
    val targetItemId: String? = null,
)
