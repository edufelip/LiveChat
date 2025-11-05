package com.project.livechat.domain.useCases

import com.project.livechat.domain.repositories.IOnboardingStatusRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingStatusUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.onboardingComplete
}
