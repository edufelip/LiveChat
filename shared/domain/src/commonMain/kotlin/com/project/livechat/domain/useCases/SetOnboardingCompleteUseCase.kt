package com.project.livechat.domain.useCases

import com.project.livechat.domain.repositories.IOnboardingStatusRepository

class SetOnboardingCompleteUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    suspend operator fun invoke(complete: Boolean) {
        repository.setOnboardingComplete(complete)
    }
}
