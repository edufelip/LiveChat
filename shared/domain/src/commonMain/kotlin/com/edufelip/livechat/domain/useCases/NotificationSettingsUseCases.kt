package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.repositories.INotificationSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationSettingsUseCase(
    private val repository: INotificationSettingsRepository,
) {
    operator fun invoke(): Flow<NotificationSettings> = repository.observeSettings()
}

class UpdatePushNotificationsUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updatePushEnabled(enabled)
    }
}

class UpdateNotificationSoundUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(sound: String) {
        repository.updateSound(sound)
    }
}

class UpdateQuietHoursEnabledUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updateQuietHoursEnabled(enabled)
    }
}

class UpdateQuietHoursWindowUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(
        from: String,
        to: String,
    ) {
        repository.updateQuietHoursWindow(from, to)
    }
}

class UpdateInAppVibrationUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updateInAppVibration(enabled)
    }
}

class UpdateMessagePreviewUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updateShowMessagePreview(enabled)
    }
}

class ResetNotificationSettingsUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke() {
        repository.resetSettings()
    }
}
