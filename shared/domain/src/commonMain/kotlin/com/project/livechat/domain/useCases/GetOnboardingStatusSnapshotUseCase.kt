package com.project.livechat.domain.useCases

import com.project.livechat.domain.repositories.IOnboardingStatusRepository

class GetOnboardingStatusSnapshotUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    operator fun invoke(): Boolean = repository.currentStatus()
}
