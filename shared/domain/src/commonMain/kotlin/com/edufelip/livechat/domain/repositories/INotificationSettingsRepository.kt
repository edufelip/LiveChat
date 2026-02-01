package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface INotificationSettingsRepository {
    fun observeSettings(): Flow<NotificationSettings>

    suspend fun updatePushEnabled(enabled: Boolean)

    suspend fun updateQuietHoursEnabled(enabled: Boolean)

    suspend fun updateQuietHoursWindow(
        from: String,
        to: String,
    )

    suspend fun updateShowMessagePreview(enabled: Boolean)
}
