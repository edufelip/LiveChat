package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppPresenterTest {
    @Test
    fun onboardingFlowReflectsRepositoryUpdates() =
        runTest {
            val repository = FakeOnboardingRepository(initiallyComplete = false)
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                repository.setOnboardingComplete(true)
                presenterScope.advanceUntilIdle()

                assertTrue(presenter.state.value.isOnboardingComplete)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun initialSnapshotSeedsAppUiState() =
        runTest {
            val repository = FakeOnboardingRepository(initiallyComplete = true)
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                assertTrue(presenter.state.value.isOnboardingComplete)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun onOnboardingFinished_marksOnboardingComplete() =
        runTest {
            val repository = FakeOnboardingRepository(initiallyComplete = false)
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.onOnboardingFinished()
                presenterScope.advanceUntilIdle()

                assertTrue(repository.currentValue)
                assertTrue(presenter.state.value.isOnboardingComplete)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun homeStateTransitionsFollowEvents() =
        runTest {
            val repository = FakeOnboardingRepository(initiallyComplete = false)
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.selectTab(HomeTab.Contacts)
                presenterScope.advanceUntilIdle()
                assertEquals(HomeTab.Contacts, presenter.state.value.home.selectedTab)
                assertEquals(null, presenter.state.value.home.activeConversationId)

                presenter.selectTab(HomeTab.Settings)
                presenterScope.advanceUntilIdle()
                assertEquals(HomeTab.Settings, presenter.state.value.home.selectedTab)
                assertEquals(null, presenter.state.value.home.activeConversationId)

                presenter.openConversation("conversation-123")
                presenterScope.advanceUntilIdle()
                assertEquals("conversation-123", presenter.state.value.home.activeConversationId)

                presenter.closeConversation()
                presenterScope.advanceUntilIdle()
                assertEquals(null, presenter.state.value.home.activeConversationId)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun startConversationWithConversationIdUsesProvidedValue() =
        runTest {
            val repository = FakeOnboardingRepository(initiallyComplete = true)
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.startConversationWith(
                    Contact(
                        id = 1,
                        name = "Ava",
                        phoneNo = "+1 (555) 010-2000",
                        isRegistered = true,
                    ),
                    conversationId = "conversation-001",
                )
                presenterScope.advanceUntilIdle()
                assertEquals("conversation-001", presenter.state.value.home.activeConversationId)
            } finally {
                presenter.close()
            }
        }

    private fun newPresenter(
        repository: FakeOnboardingRepository,
        scope: TestScope,
    ) = AppPresenter(
        observeOnboardingStatus = ObserveOnboardingStatusUseCase(repository),
        setOnboardingComplete = SetOnboardingCompleteUseCase(repository),
        getOnboardingStatusSnapshot = GetOnboardingStatusSnapshotUseCase(repository),
        scope = scope,
    )

    private class FakeOnboardingRepository(
        initiallyComplete: Boolean,
    ) : IOnboardingStatusRepository {
        private val state = MutableStateFlow(initiallyComplete)

        override val onboardingComplete: Flow<Boolean>
            get() = state

        override suspend fun setOnboardingComplete(complete: Boolean) {
            state.value = complete
        }

        override fun currentStatus(): Boolean = state.value

        val currentValue: Boolean
            get() = state.value
    }
}
