package com.edufelip.livechat.data.store

import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.repositories.IPrivacySettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrivacySettingsStore(
    repository: IPrivacySettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val settingsState = MutableStateFlow(PrivacySettings())
    val settings = settingsState.asStateFlow()

    init {
        scope.launch {
            repository
                .observeSettings()
                .catch { throwable ->
                    println("PrivacySettingsStore: observe failed ${throwable.message}")
                }.collectLatest { settings ->
                    settingsState.value = settings
                }
        }
    }

    fun currentSettings(): PrivacySettings = settingsState.value

    fun lastSeenAudience(): LastSeenAudience = settingsState.value.lastSeenAudience

    fun readReceiptsEnabled(): Boolean = settingsState.value.readReceiptsEnabled
}
