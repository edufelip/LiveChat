package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IPrivacySettingsRemoteData
import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IPrivacySettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class PrivacySettingsRepository(
    private val remoteData: IPrivacySettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IPrivacySettingsRepository {
    override fun observeSettings(): Flow<PrivacySettings> =
        sessionProvider.session
            .mapLatest { session ->
                if (session == null) return@mapLatest PrivacySettings()
                remoteData.fetchSettings(session.userId, session.idToken) ?: PrivacySettings()
            }
            .flowOn(dispatcher)

    override suspend fun updateInvitePreference(preference: InvitePreference) {
        val session = requireSession()
        remoteData.updateInvitePreference(session.userId, session.idToken, preference)
    }

    override suspend fun updateLastSeenAudience(audience: LastSeenAudience) {
        val session = requireSession()
        remoteData.updateLastSeenAudience(session.userId, session.idToken, audience)
    }

    override suspend fun updateReadReceipts(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateReadReceipts(session.userId, session.idToken, enabled)
    }

    override suspend fun updateShareUsageData(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateShareUsageData(session.userId, session.idToken, enabled)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
