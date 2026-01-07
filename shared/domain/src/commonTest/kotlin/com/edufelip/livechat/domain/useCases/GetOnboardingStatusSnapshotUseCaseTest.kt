package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class GetOnboardingStatusSnapshotUseCaseTest {
    @Test
    fun `returns repository snapshot value`() {
        val repository = FakeRepository(initiallyComplete = true)
        val useCase = GetOnboardingStatusSnapshotUseCase(repository)

        assertEquals(true, useCase())

        repository.state = false
        assertEquals(false, useCase())
    }

    private class FakeRepository(
        initiallyComplete: Boolean,
    ) : IOnboardingStatusRepository {
        var state: Boolean = initiallyComplete
        var welcomeSeenState: Boolean = false

        override val onboardingComplete: Flow<Boolean> = emptyFlow()
        override val welcomeSeen: Flow<Boolean> = emptyFlow()

        override suspend fun setOnboardingComplete(complete: Boolean) {
            state = complete
        }

        override suspend fun setWelcomeSeen(seen: Boolean) {
            welcomeSeenState = seen
        }

        override fun currentStatus(): Boolean = state

        override fun currentWelcomeSeen(): Boolean = welcomeSeenState
    }
}
