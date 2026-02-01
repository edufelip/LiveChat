package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.domain.useCases.ObservePrivacySettingsUseCase
import com.edufelip.livechat.domain.useCases.ResetPrivacySettingsUseCase
import com.edufelip.livechat.domain.useCases.UpdateInvitePreferenceUseCase
import com.edufelip.livechat.domain.useCases.UpdateLastSeenAudienceUseCase
import com.edufelip.livechat.domain.useCases.UpdateReadReceiptsUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrivacySettingsPresenter(
    private val observeSettings: ObservePrivacySettingsUseCase,
    private val updateInvitePreference: UpdateInvitePreferenceUseCase,
    private val updateLastSeenAudience: UpdateLastSeenAudienceUseCase,
    private val updateReadReceipts: UpdateReadReceiptsUseCase,
    private val resetSettings: ResetPrivacySettingsUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(PrivacySettingsUiState())
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<PrivacySettingsUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeSettings()
                .catch { throwable ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load privacy settings",
                        )
                    }
                }
                .collectLatest { settings ->
                    mutableState.update {
                        it.copy(isLoading = false, settings = settings, errorMessage = null)
                    }
                }
        }
    }

    fun updateInvitePreference(preference: InvitePreference) {
        updateSettings(
            update = { updateInvitePreference(preference) },
            localUpdate = { it.copy(invitePreference = preference) },
        )
    }

    fun updateLastSeenAudience(audience: LastSeenAudience) {
        updateSettings(
            update = { updateLastSeenAudience(audience) },
            localUpdate = { it.copy(lastSeenAudience = audience) },
        )
    }

    fun updateReadReceipts(enabled: Boolean) {
        updateSettings(
            update = { updateReadReceipts(enabled) },
            localUpdate = { it.copy(readReceiptsEnabled = enabled) },
        )
    }

    fun resetPrivacySettings() {
        updateSettings(
            update = { resetSettings() },
            localUpdate = { PrivacySettings() },
        )
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun updateSettings(
        update: suspend () -> Unit,
        localUpdate: (PrivacySettings) -> PrivacySettings,
    ) {
        if (mutableState.value.isUpdating) return
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { update() }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            settings = localUpdate(state.settings),
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Update failed",
                        )
                    }
                }
        }
    }
}
