package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.AppUiState
import com.project.livechat.domain.models.HomeTab
import com.project.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.project.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppPresenter(
    observeOnboardingStatus: ObserveOnboardingStatusUseCase,
    private val setOnboardingComplete: SetOnboardingCompleteUseCase,
    private val scope: CoroutineScope = MainScope(),
) {

    private val mutableState = MutableStateFlow(AppUiState())
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<AppUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeOnboardingStatus()
                .collectLatest { isComplete ->
                    mutableState.update { current ->
                        current.copy(isOnboardingComplete = isComplete)
                    }
                }
        }
    }

    fun onOnboardingFinished() {
        scope.launch {
            setOnboardingComplete(true)
        }
    }

    fun selectTab(tab: HomeTab) {
        mutableState.update { current ->
            if (current.home.selectedTab == tab && current.home.activeConversationId == null) {
                current
            } else {
                current.copy(home = current.home.copy(selectedTab = tab, activeConversationId = null))
            }
        }
    }

    fun openConversation(conversationId: String) {
        mutableState.update { current ->
            if (current.home.activeConversationId == conversationId) {
                current
            } else {
                current.copy(home = current.home.copy(activeConversationId = conversationId))
            }
        }
    }

    fun closeConversation() {
        mutableState.update { current ->
            if (current.home.activeConversationId == null) {
                current
            } else {
                current.copy(home = current.home.copy(activeConversationId = null))
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
