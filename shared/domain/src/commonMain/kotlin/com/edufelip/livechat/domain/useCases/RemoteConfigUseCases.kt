package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IRemoteConfigRepository
import kotlinx.coroutines.flow.Flow

class ObservePrivacyPolicyUrlUseCase(
    private val repository: IRemoteConfigRepository,
) {
    operator fun invoke(): Flow<String> = repository.observePrivacyPolicyUrl()
}

class RefreshRemoteConfigUseCase(
    private val repository: IRemoteConfigRepository,
) {
    suspend operator fun invoke() {
        repository.refresh()
    }
}
