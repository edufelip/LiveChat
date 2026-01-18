package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.INotificationSettingsRemoteData
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.INotificationSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationSettingsRepository(
    private val remoteData: INotificationSettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : INotificationSettingsRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val settingsState = MutableStateFlow(NotificationSettings())

    init {
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                if (session == null) {
                    settingsState.value = NotificationSettings()
                    return@collectLatest
                }
                val settings =
                    runCatching { remoteData.fetchSettings(session.userId, session.idToken) }
                        .getOrNull()
                        ?: NotificationSettings()
                settingsState.value = settings
            }
        }
    }

    override fun observeSettings(): Flow<NotificationSettings> = settingsState.asStateFlow()

    override suspend fun updatePushEnabled(enabled: Boolean) {
        val session = requireSession()
        remoteData.updatePushEnabled(session.userId, session.idToken, enabled)
        settingsState.value = settingsState.value.copy(pushEnabled = enabled)
    }

    override suspend fun updateSound(sound: String) {
        val session = requireSession()
        val normalizedSound = NotificationSound.normalizeId(sound)
        remoteData.updateSound(session.userId, session.idToken, normalizedSound)
        settingsState.value = settingsState.value.copy(sound = normalizedSound)
    }

    override suspend fun updateQuietHoursEnabled(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateQuietHoursEnabled(session.userId, session.idToken, enabled)
        settingsState.value = settingsState.value.copy(quietHoursEnabled = enabled)
    }

    override suspend fun updateQuietHoursWindow(
        from: String,
        to: String,
    ) {
        val session = requireSession()
        remoteData.updateQuietHoursWindow(session.userId, session.idToken, from, to)
        settingsState.value =
            settingsState.value.copy(
                quietHours = settingsState.value.quietHours.copy(from = from, to = to),
            )
    }

    override suspend fun updateInAppVibration(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateInAppVibration(session.userId, session.idToken, enabled)
        settingsState.value = settingsState.value.copy(inAppVibration = enabled)
    }

    override suspend fun updateShowMessagePreview(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateShowMessagePreview(session.userId, session.idToken, enabled)
        settingsState.value = settingsState.value.copy(showMessagePreview = enabled)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
        settingsState.value = NotificationSettings()
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
