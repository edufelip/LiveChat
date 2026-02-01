package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.repositories.IPrivacySettingsRepository
import kotlinx.coroutines.flow.Flow

class ObservePrivacySettingsUseCase(
    private val repository: IPrivacySettingsRepository,
) {
    operator fun invoke(): Flow<PrivacySettings> = repository.observeSettings()
}

class UpdateInvitePreferenceUseCase(
    private val repository: IPrivacySettingsRepository,
) {
    suspend operator fun invoke(preference: InvitePreference) {
        repository.updateInvitePreference(preference)
    }
}

class UpdateLastSeenAudienceUseCase(
    private val repository: IPrivacySettingsRepository,
) {
    suspend operator fun invoke(audience: LastSeenAudience) {
        repository.updateLastSeenAudience(audience)
    }
}

class UpdateReadReceiptsUseCase(
    private val repository: IPrivacySettingsRepository,
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.updateReadReceipts(enabled)
        if (!enabled) {
            messagesRepository.hideReadReceipts()
        }
    }
}

class ResetPrivacySettingsUseCase(
    private val repository: IPrivacySettingsRepository,
) {
    suspend operator fun invoke() {
        repository.resetSettings()
    }
}
