package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.EnsureConversationUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.ResolveConversationIdForContactUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.ContactsSyncSession
import com.edufelip.livechat.domain.utils.ContactsUiStateCache
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsPresenter(
    private val getLocalContactsUseCase: GetLocalContactsUseCase,
    private val checkRegisteredContactsUseCase: CheckRegisteredContactsUseCase,
    private val resolveConversationIdForContactUseCase: ResolveConversationIdForContactUseCase,
    private val ensureConversationUseCase: EnsureConversationUseCase,
    private val phoneNumberFormatter: PhoneNumberFormatter,
    private val scope: CoroutineScope,
) {
    private val mutableState =
        MutableStateFlow(
            ContactsUiStateCache.snapshot()
                ?: ContactsUiState(isLoading = !ContactsUiStateCache.hasVisitedContacts()),
        )
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<ContactsUiState> = state.asCStateFlow()
    private val mutableEvents = MutableSharedFlow<ContactsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ContactsEvent> = mutableEvents
    private var lastSuccessfulFingerprint: String? = null
    private var syncInFlight: Boolean = false

    init {
        ContactsUiStateCache.markVisited()
        scope.launch {
            getLocalContactsUseCase()
                .catch { throwable ->
                    updateState { it.copy(isLoading = false, errorMessage = throwable.message) }
                }.collectLatest { contacts ->
                    updateState { state ->
                        state.copy(
                            localContacts = contacts,
                            validatedContacts = contacts.filter { it.isRegistered },
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun shouldSyncContacts(
        phoneContacts: List<Contact>,
        force: Boolean = false,
    ): Boolean {
        if (force) return true
        if (syncInFlight) return false
        if (phoneContacts.isEmpty()) return false
        val fingerprint = phoneContacts.fingerprint()
        return fingerprint != lastSuccessfulFingerprint || ContactsSyncSession.canSync()
    }

    fun syncContacts(
        phoneContacts: List<Contact>,
        force: Boolean = false,
    ) {
        val targetFingerprint = phoneContacts.fingerprint()
        if (syncInFlight) return
        if (!force && targetFingerprint == lastSuccessfulFingerprint && !ContactsSyncSession.canSync()) return

        if (phoneContacts.isEmpty()) return
        syncInFlight = true
        scope.launch {
            val localContacts = mutableState.value.localContacts
            updateState { it.copy(isSyncing = true, errorMessage = null) }
            var hadError = false
            try {
                checkRegisteredContactsUseCase(phoneContacts, localContacts)
                    .catch { throwable ->
                        hadError = true
                        updateState { it.copy(isSyncing = false, errorMessage = throwable.message) }
                    }.collect { contact ->
                        updateState { state ->
                            val updated =
                                (state.validatedContacts + contact).distinctBy {
                                    phoneNumberFormatter.normalize(it.phoneNo)
                                }
                            state.copy(validatedContacts = updated)
                        }
                    }
            } catch (throwable: Throwable) {
                hadError = true
                updateState { it.copy(isSyncing = false, errorMessage = throwable.message) }
            } finally {
                updateState { it.copy(isSyncing = false) }
                if (!hadError) {
                    lastSuccessfulFingerprint = targetFingerprint
                    ContactsSyncSession.markSynced()
                }
                syncInFlight = false
            }
        }
    }

    fun inviteContact(contact: Contact) {
        // No-op: invite flow handled on the platform layer. Presenter intentionally left empty.
    }

    fun setSearchQuery(query: String) {
        updateState { state ->
            val trimmed = query.trim()
            if (state.searchQuery == trimmed) {
                state
            } else {
                state.copy(searchQuery = trimmed)
            }
        }
    }

    fun onContactSelected(contact: Contact) {
        scope.launch {
            if (!contact.isRegistered || contact.firebaseUid.isNullOrBlank()) {
                updateState { it.copy(errorMessage = "Contact is not available right now") }
                return@launch
            }
            val conversationId = resolveConversationIdForContactUseCase(contact)
            if (conversationId.isBlank()) {
                updateState { it.copy(errorMessage = "Unable to open conversation") }
                return@launch
            }
            val peer = ConversationPeer(firebaseUid = contact.firebaseUid, phoneNumber = contact.phoneNo)
            val emitted = mutableEvents.tryEmit(ContactsEvent.OpenConversation(contact, conversationId))
            if (!emitted) {
                mutableEvents.emit(ContactsEvent.OpenConversation(contact, conversationId))
            }

            scope.launch {
                runCatching { ensureConversationUseCase(conversationId, peer) }
                    .onFailure { throwable ->
                        updateState { it.copy(errorMessage = throwable.message ?: "Failed to open conversation") }
                    }
            }
        }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun List<Contact>.fingerprint(): String =
        asSequence()
            .map { phoneNumberFormatter.normalize(it.phoneNo) }
            .filter { it.isNotBlank() }
            .sorted()
            .joinToString("#")

    private fun updateState(transform: (ContactsUiState) -> ContactsUiState) {
        mutableState.update { current ->
            val updated = transform(current)
            ContactsUiStateCache.update(updated)
            updated
        }
    }
}

sealed interface ContactsEvent {
    data class ShareInvite(
        val contact: Contact,
        val message: String,
    ) : ContactsEvent

    data class OpenConversation(
        val contact: Contact,
        val conversationId: String,
    ) : ContactsEvent
}
