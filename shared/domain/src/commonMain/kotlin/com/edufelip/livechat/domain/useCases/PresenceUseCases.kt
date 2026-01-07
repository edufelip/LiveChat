package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.PresenceState
import com.edufelip.livechat.domain.repositories.IPresenceRepository
import kotlinx.coroutines.flow.Flow

class ObservePresenceUseCase(
    private val repository: IPresenceRepository,
) {
    operator fun invoke(userIds: List<String>): Flow<Map<String, PresenceState>> = repository.observePresence(userIds)
}

class UpdateSelfPresenceUseCase(
    private val repository: IPresenceRepository,
) {
    suspend operator fun invoke(isOnline: Boolean) {
        repository.updateSelfPresence(isOnline)
    }
}
