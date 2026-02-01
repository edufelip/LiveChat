package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IAppearanceSettingsRemoteData
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IAppearanceSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppearanceSettingsRepository(
    private val remoteData: IAppearanceSettingsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAppearanceSettingsRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val settingsState = MutableStateFlow(AppearanceSettings())

    init {
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                if (session == null) {
                    settingsState.value = AppearanceSettings()
                    return@collectLatest
                }
                val settings =
                    runCatching { remoteData.fetchSettings(session.userId, session.idToken) }
                        .getOrNull()
                        ?: AppearanceSettings()
                settingsState.value = settings
            }
        }
    }

    override fun observeSettings(): Flow<AppearanceSettings> = settingsState.asStateFlow()

    override suspend fun updateThemeMode(mode: ThemeMode) {
        val session = requireSession()
        remoteData.updateThemeMode(session.userId, session.idToken, mode)
        settingsState.value = settingsState.value.copy(themeMode = mode)
    }

    override suspend fun updateTextScale(scale: Float) {
        val session = requireSession()
        val clamped = scale.coerceIn(AppearanceSettings.MIN_TEXT_SCALE, AppearanceSettings.MAX_TEXT_SCALE)
        remoteData.updateTextScale(session.userId, session.idToken, clamped)
        settingsState.value = settingsState.value.copy(textScale = clamped)
    }

    override suspend fun resetSettings() {
        val session = requireSession()
        remoteData.resetSettings(session.userId, session.idToken)
        settingsState.value = AppearanceSettings()
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
