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

class UpdateMessagePreviewUseCase(
    private val repository: INotificationSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updateShowMessagePreview(enabled)
    }
}
