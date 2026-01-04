package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IAppearanceSettingsRemoteData
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IAppearanceSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class AppearanceSettingsRepository(
    private val remoteData: IAppearanceSettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAppearanceSettingsRepository {
    override fun observeSettings(): Flow<AppearanceSettings> =
        sessionProvider.session
            .mapLatest { session ->
                if (session == null) return@mapLatest AppearanceSettings()
                remoteData.fetchSettings(session.userId, session.idToken) ?: AppearanceSettings()
            }
            .flowOn(dispatcher)

    override suspend fun updateThemeMode(mode: ThemeMode) {
        val session = requireSession()
        remoteData.updateThemeMode(session.userId, session.idToken, mode)
    }

    override suspend fun updateTextScale(scale: Float) {
        val session = requireSession()
        val clamped = scale.coerceIn(AppearanceSettings.MIN_TEXT_SCALE, AppearanceSettings.MAX_TEXT_SCALE)
        remoteData.updateTextScale(session.userId, session.idToken, clamped)
    }

    override suspend fun updateReduceMotion(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateReduceMotion(session.userId, session.idToken, enabled)
    }

    override suspend fun updateHighContrast(enabled: Boolean) {
        val session = requireSession()
        remoteData.updateHighContrast(session.userId, session.idToken, enabled)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
