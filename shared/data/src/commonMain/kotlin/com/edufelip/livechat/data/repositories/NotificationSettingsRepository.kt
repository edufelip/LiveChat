package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.INotificationSettingsRemoteData
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.INotificationSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class NotificationSettingsRepository(
    private val remoteData: INotificationSettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : INotificationSettingsRepository {
    override fun observeSettings(): Flow<NotificationSettings> =
        sessionProvider.session
            .mapLatest { session ->
                if (session == null) return@mapLatest NotificationSettings()
                remoteData.fetchSettings(session.userId, session.idToken) ?: NotificationSettings()
            }
            .flowOn(dispatcher)

    override suspend fun updatePushEnabled(enabled: Boolean) {
        val session = requireSession()
        remoteData.updatePushEnabled(session.userId, session.idToken, enabled)
    }

    override suspend fun updateSound(sound: String) {
        val session = requireSession()
        remoteData.updateSound(session.userId, session.idToken, sound)
    }

    override suspend fun updateQuietHoursEnabled(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateQuietHoursEnabled(session.userId, session.idToken, enabled)
    }

    override suspend fun updateQuietHoursWindow(
        from: String,
        to: String,
    ) {
        val session = requireSession()
        remoteData.updateQuietHoursWindow(session.userId, session.idToken, from, to)
    }

    override suspend fun updateInAppVibration(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateInAppVibration(session.userId, session.idToken, enabled)
    }

    override suspend fun updateShowMessagePreview(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateShowMessagePreview(session.userId, session.idToken, enabled)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
