package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.HomeTab
import com.project.livechat.domain.repositories.IOnboardingStatusRepository
import com.project.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.project.livechat.domain.useCases.SetOnboardingCompleteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppPresenterTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private lateinit var repository: FakeOnboardingRepository
    private lateinit var presenter: AppPresenter

    @BeforeTest
    fun setUp() {
        repository = FakeOnboardingRepository(initiallyComplete = false)
        presenter =
            AppPresenter(
                observeOnboardingStatus = ObserveOnboardingStatusUseCase(repository),
                setOnboardingComplete = SetOnboardingCompleteUseCase(repository),
                scope = scope,
            )
    }

    @AfterTest
    fun tearDown() {
        presenter.close()
    }

    @Test
    fun onboardingFlowReflectsRepositoryUpdates() =
        scope.runTest {
            repository.setOnboardingComplete(true)
            advanceUntilIdle()

            assertTrue(presenter.state.value.isOnboardingComplete)
        }

    @Test
    fun onOnboardingFinished_marksOnboardingComplete() =
        scope.runTest {
            presenter.onOnboardingFinished()
            advanceUntilIdle()

            assertTrue(repository.currentValue)
            assertTrue(presenter.state.value.isOnboardingComplete)
        }

    @Test
    fun homeStateTransitionsFollowEvents() =
        scope.runTest {
            presenter.selectTab(HomeTab.Contacts)
            assertEquals(HomeTab.Contacts, presenter.state.value.home.selectedTab)
            assertEquals(null, presenter.state.value.home.activeConversationId)

            presenter.openConversation("conversation-123")
            assertEquals("conversation-123", presenter.state.value.home.activeConversationId)

            presenter.closeConversation()
            assertEquals(null, presenter.state.value.home.activeConversationId)
        }

    private class FakeOnboardingRepository(
        initiallyComplete: Boolean,
    ) : IOnboardingStatusRepository {
        private val state = MutableStateFlow(initiallyComplete)

        override val onboardingComplete: Flow<Boolean>
            get() = state

        override suspend fun setOnboardingComplete(complete: Boolean) {
            state.value = complete
        }

        val currentValue: Boolean
            get() = state.value
    }
}
