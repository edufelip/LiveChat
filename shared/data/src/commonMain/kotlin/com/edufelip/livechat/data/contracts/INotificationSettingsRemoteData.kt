package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.NotificationSettings

interface INotificationSettingsRemoteData {
    suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): NotificationSettings?

    suspend fun updatePushEnabled(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )

    suspend fun updateQuietHoursEnabled(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )

    suspend fun updateQuietHoursWindow(
        userId: String,
        idToken: String,
        from: String,
        to: String,
    )

    suspend fun updateShowMessagePreview(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )
}
