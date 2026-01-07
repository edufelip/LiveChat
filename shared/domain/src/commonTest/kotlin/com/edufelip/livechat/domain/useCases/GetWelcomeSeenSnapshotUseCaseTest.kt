package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class GetWelcomeSeenSnapshotUseCaseTest {
    @Test
    fun `returns repository snapshot value`() {
        val repository = FakeRepository(initiallySeen = true)
        val useCase = GetWelcomeSeenSnapshotUseCase(repository)

        assertEquals(true, useCase())

        repository.seen = false
        assertEquals(false, useCase())
    }

    private class FakeRepository(
        initiallySeen: Boolean,
    ) : IOnboardingStatusRepository {
        var seen: Boolean = initiallySeen

        override val onboardingComplete: Flow<Boolean> = emptyFlow()
        override val welcomeSeen: Flow<Boolean> = emptyFlow()

        override suspend fun setOnboardingComplete(complete: Boolean) = Unit

        override suspend fun setWelcomeSeen(seen: Boolean) {
            this.seen = seen
        }

        override fun currentStatus(): Boolean = false

        override fun currentWelcomeSeen(): Boolean = seen
    }
}
