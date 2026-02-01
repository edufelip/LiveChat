package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IPrivacySettingsRemoteData
import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IPrivacySettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrivacySettingsRepository(
    private val remoteData: IPrivacySettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IPrivacySettingsRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val settingsState = MutableStateFlow(PrivacySettings())

    init {
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                if (session == null) {
                    settingsState.value = PrivacySettings()
                    return@collectLatest
                }
                val settings =
                    runCatching { remoteData.fetchSettings(session.userId, session.idToken) }
                        .getOrNull()
                        ?: PrivacySettings()
                settingsState.value = settings
            }
        }
    }

    override fun observeSettings(): Flow<PrivacySettings> = settingsState.asStateFlow()

    override suspend fun updateInvitePreference(preference: InvitePreference) {
        val session = requireSession()
        remoteData.updateInvitePreference(session.userId, session.idToken, preference)
        settingsState.value = settingsState.value.copy(invitePreference = preference)
    }

    override suspend fun updateLastSeenAudience(audience: LastSeenAudience) {
        val session = requireSession()
        remoteData.updateLastSeenAudience(session.userId, session.idToken, audience)
        settingsState.value = settingsState.value.copy(lastSeenAudience = audience)
    }

    override suspend fun updateReadReceipts(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateReadReceipts(session.userId, session.idToken, enabled)
        settingsState.value = settingsState.value.copy(readReceiptsEnabled = enabled)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
        settingsState.value = PrivacySettings()
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
