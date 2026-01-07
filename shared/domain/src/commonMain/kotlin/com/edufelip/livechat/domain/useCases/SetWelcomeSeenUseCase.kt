package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository

class SetWelcomeSeenUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    suspend operator fun invoke(seen: Boolean) {
        repository.setWelcomeSeen(seen)
    }
}
