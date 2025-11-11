package com.edufelip.livechat.ui.features.settings.model

import com.edufelip.livechat.ui.features.settings.screens.SettingsSection

data class SettingsNavigationRequest(
    val section: SettingsSection,
    val title: String,
    val description: String,
    val placeholderMessage: String,
)
