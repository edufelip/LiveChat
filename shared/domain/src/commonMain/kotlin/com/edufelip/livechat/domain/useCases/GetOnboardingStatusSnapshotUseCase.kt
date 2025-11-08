package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository

class GetOnboardingStatusSnapshotUseCase(
    private val repository: IOnboardingStatusRepository,
) {
    operator fun invoke(): Boolean = repository.currentStatus()
}
