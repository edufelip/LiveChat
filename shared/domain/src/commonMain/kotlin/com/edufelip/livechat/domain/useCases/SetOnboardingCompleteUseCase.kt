package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository

class SetOnboardingCompleteUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    suspend operator fun invoke(complete: Boolean) {
        repository.setOnboardingComplete(complete)
    }
}
