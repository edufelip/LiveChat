package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository

class GetWelcomeSeenSnapshotUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    operator fun invoke(): Boolean = repository.currentWelcomeSeen()
}
