package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AppUiState
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppPresenter(
    observeOnboardingStatus: ObserveOnboardingStatusUseCase,
    observeConversationUseCase: ObserveConversationUseCase,
    private val setOnboardingComplete: SetOnboardingCompleteUseCase,
    getOnboardingStatusSnapshot: GetOnboardingStatusSnapshotUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(AppUiState(isOnboardingComplete = getOnboardingStatusSnapshot()))
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
        scope.launch {
            observeConversationUseCase.observeAll().collectLatest {
                // Messages are persisted by repository; no UI state change needed here.
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
                current.copy(home = current.home.copy(selectedTab = tab, activeConversationId = null, activeConversationName = null))
            }
        }
    }

    fun startConversationWith(
        contact: Contact,
        conversationId: String,
    ) {
        if (conversationId.isBlank()) return
        openConversation(conversationId, contact.name.ifBlank { contact.phoneNo })
    }

    fun openConversation(
        conversationId: String,
        contactName: String? = null,
    ) {
        mutableState.update { current ->
            if (current.home.activeConversationId == conversationId && contactName == null) {
                current
            } else {
                current.copy(
                    home =
                        current.home.copy(
                            activeConversationId = conversationId,
                            activeConversationName = contactName ?: current.home.activeConversationName,
                        ),
                )
            }
        }
    }

    fun closeConversation() {
        mutableState.update { current ->
            if (current.home.activeConversationId == null) {
                current
            } else {
                current.copy(home = current.home.copy(activeConversationId = null, activeConversationName = null))
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
