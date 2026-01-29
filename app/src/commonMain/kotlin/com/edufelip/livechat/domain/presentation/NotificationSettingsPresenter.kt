package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.domain.useCases.ObserveNotificationSettingsUseCase
import com.edufelip.livechat.domain.useCases.ResetNotificationSettingsUseCase
import com.edufelip.livechat.domain.useCases.UpdateInAppVibrationUseCase
import com.edufelip.livechat.domain.useCases.UpdateMessagePreviewUseCase
import com.edufelip.livechat.domain.useCases.UpdateNotificationSoundUseCase
import com.edufelip.livechat.domain.useCases.UpdatePushNotificationsUseCase
import com.edufelip.livechat.domain.useCases.UpdateQuietHoursEnabledUseCase
import com.edufelip.livechat.domain.useCases.UpdateQuietHoursWindowUseCase
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

class NotificationSettingsPresenter(
    private val observeSettings: ObserveNotificationSettingsUseCase,
    private val updatePushEnabled: UpdatePushNotificationsUseCase,
    private val updateSound: UpdateNotificationSoundUseCase,
    private val updateQuietHoursEnabled: UpdateQuietHoursEnabledUseCase,
    private val updateQuietHoursWindow: UpdateQuietHoursWindowUseCase,
    private val updateInAppVibration: UpdateInAppVibrationUseCase,
    private val updateShowMessagePreview: UpdateMessagePreviewUseCase,
    private val resetSettings: ResetNotificationSettingsUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(NotificationSettingsUiState())
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<NotificationSettingsUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeSettings()
                .catch { throwable ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load notification settings",
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

    fun updatePushNotifications(enabled: Boolean) {
        updateSettings(
            update = { updatePushEnabled(enabled) },
            localUpdate = { it.copy(pushEnabled = enabled) },
        )
    }

    fun updateSound(sound: String) {
        val normalizedSound = NotificationSound.normalizeId(sound)
        if (normalizedSound.isBlank()) return
        updateSettings(
            update = { updateSound(normalizedSound) },
            localUpdate = { it.copy(sound = normalizedSound) },
        )
    }

    fun updateQuietHoursEnabled(enabled: Boolean) {
        updateSettings(
            update = { updateQuietHoursEnabled(enabled) },
            localUpdate = { it.copy(quietHoursEnabled = enabled) },
        )
    }

    fun updateQuietHoursWindow(
        from: String,
        to: String,
    ) {
        if (from.isBlank() || to.isBlank()) return
        updateSettings(
            update = { updateQuietHoursWindow(from, to) },
            localUpdate = { it.copy(quietHours = it.quietHours.copy(from = from, to = to)) },
        )
    }

    fun updateInAppVibration(enabled: Boolean) {
        updateSettings(
            update = { updateInAppVibration(enabled) },
            localUpdate = { it.copy(inAppVibration = enabled) },
        )
    }

    fun updateShowMessagePreview(enabled: Boolean) {
        updateSettings(
            update = { updateShowMessagePreview(enabled) },
            localUpdate = { it.copy(showMessagePreview = enabled) },
        )
    }

    fun resetNotificationSettings() {
        updateSettings(
            update = { resetSettings() },
            localUpdate = { NotificationSettings() },
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
        localUpdate: (NotificationSettings) -> NotificationSettings,
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
