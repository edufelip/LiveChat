package com.edufelip.livechat.ui.features.settings.model

import com.edufelip.livechat.ui.resources.SettingsStrings

enum class SettingsSection {
    Account,
    Notifications,
    Appearance,
    Privacy,
}

fun SettingsSection.title(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountTitle
        SettingsSection.Notifications -> strings.notificationsTitle
        SettingsSection.Appearance -> strings.appearanceTitle
        SettingsSection.Privacy -> strings.privacyTitle
    }

fun SettingsSection.description(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountDescription
        SettingsSection.Notifications -> strings.notificationsDescription
        SettingsSection.Appearance -> strings.appearanceDescription
        SettingsSection.Privacy -> strings.privacyDescription
    }
