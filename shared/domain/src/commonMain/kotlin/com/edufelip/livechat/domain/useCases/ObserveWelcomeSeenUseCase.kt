package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import kotlinx.coroutines.flow.Flow

class ObserveWelcomeSeenUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.welcomeSeen
}
