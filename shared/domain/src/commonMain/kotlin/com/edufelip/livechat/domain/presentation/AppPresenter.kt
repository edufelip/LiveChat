package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AppUiState
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.useCases.GetLocalContactsSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetWelcomeSeenSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObserveWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.useCases.SetWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.UpdateSelfPresenceUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.ContactsSyncSession
import com.edufelip.livechat.domain.utils.ContactsUiStateCache
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppPresenter(
    observeOnboardingStatus: ObserveOnboardingStatusUseCase,
    observeWelcomeSeen: ObserveWelcomeSeenUseCase,
    observeConversationUseCase: ObserveConversationUseCase,
    private val setOnboardingComplete: SetOnboardingCompleteUseCase,
    private val setWelcomeSeen: SetWelcomeSeenUseCase,
    getOnboardingStatusSnapshot: GetOnboardingStatusSnapshotUseCase,
    getWelcomeSeenSnapshot: GetWelcomeSeenSnapshotUseCase,
    getLocalContactsSnapshot: GetLocalContactsSnapshotUseCase,
    private val updateSelfPresence: UpdateSelfPresenceUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState =
        MutableStateFlow(
            AppUiState(
                isOnboardingComplete = getOnboardingStatusSnapshot(),
                hasSeenWelcome = getWelcomeSeenSnapshot(),
            ),
        )
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<AppUiState> = state.asCStateFlow()
    private var presenceJob: Job? = null

    init {
        ContactsSyncSession.markAppOpen()
        scope.launch {
            runCatching {
                val snapshot = withContext(Dispatchers.Default) { getLocalContactsSnapshot() }
                ContactsUiStateCache.seedFromSnapshot(snapshot)
            }
        }
        scope.launch {
            observeOnboardingStatus()
                .collectLatest { isComplete ->
                    mutableState.update { current ->
                        current.copy(isOnboardingComplete = isComplete)
                    }
                }
        }
        scope.launch {
            observeWelcomeSeen()
                .collectLatest { hasSeen ->
                    mutableState.update { current ->
                        current.copy(hasSeenWelcome = hasSeen)
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
            setWelcomeSeen(true)
        }
    }

    fun onWelcomeFinished() {
        mutableState.update { current -> current.copy(hasSeenWelcome = true) }
        scope.launch {
            setWelcomeSeen(true)
        }
    }

    fun resetOnboarding() {
        mutableState.update { current ->
            current.copy(
                isOnboardingComplete = false,
                hasSeenWelcome = false,
            )
        }
        scope.launch {
            setOnboardingComplete(false)
            setWelcomeSeen(false)
        }
    }

    fun selectTab(tab: HomeTab) {
        mutableState.update { current ->
            if (current.home.selectedTab == tab && current.home.activeConversationId == null) {
                current
            } else {
                current.copy(
                    home =
                        current.home.copy(
                            selectedTab = tab,
                            activeConversationId = null,
                            activeConversationName = null,
                            isContactsVisible = false,
                        ),
                )
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
                            isContactsVisible = false,
                        ),
                )
            }
        }
    }

    fun openContacts() {
        mutableState.update { current ->
            current.copy(home = current.home.copy(isContactsVisible = true))
        }
    }

    fun closeContacts() {
        mutableState.update { current ->
            current.copy(home = current.home.copy(isContactsVisible = false))
        }
    }

    fun closeConversation() {
        mutableState.update { current ->
            if (current.home.activeConversationId == null) {
                current
            } else {
                current.copy(
                    home =
                        current.home.copy(
                            activeConversationId = null,
                            activeConversationName = null,
                        ),
                )
            }
        }
    }

    fun onAppForeground() {
        presenceJob?.cancel()
        presenceJob =
            scope.launch {
                updateSelfPresence(true)
                while (isActive) {
                    delay(PRESENCE_HEARTBEAT_MS)
                    updateSelfPresence(true)
                }
            }
    }

    fun onAppBackground() {
        presenceJob?.cancel()
        presenceJob = null
        scope.launch {
            updateSelfPresence(false)
        }
    }

    fun close() {
        presenceJob?.cancel()
        scope.cancel()
    }

    private companion object {
        const val PRESENCE_HEARTBEAT_MS = 60_000L
    }
}
