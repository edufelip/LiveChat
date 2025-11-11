package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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
    private val scope: CoroutineScope = MainScope(),
) {
    private val mutableState = MutableStateFlow(ContactsUiState(isLoading = true))
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<ContactsUiState> = state.asCStateFlow()
    private val mutableEvents = MutableSharedFlow<ContactsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ContactsEvent> = mutableEvents
    private var lastSyncedFingerprint: String? = null

    init {
        scope.launch {
            getLocalContactsUseCase()
                .catch { throwable ->
                    mutableState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                }
                .collectLatest { contacts ->
                    mutableState.update { state ->
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
        val fingerprint = phoneContacts.fingerprint()
        return fingerprint != lastSyncedFingerprint
    }

    fun syncContacts(
        phoneContacts: List<Contact>,
        force: Boolean = false,
    ) {
        val targetFingerprint = phoneContacts.fingerprint()
        if (!force && targetFingerprint == lastSyncedFingerprint) return

        scope.launch {
            val localContacts = mutableState.value.localContacts
            mutableState.update { it.copy(isSyncing = true, errorMessage = null) }
            runCatching {
                checkRegisteredContactsUseCase(phoneContacts, localContacts)
            }.onSuccess { flow ->
                flow
                    .catch { throwable ->
                        mutableState.update { it.copy(isSyncing = false, errorMessage = throwable.message) }
                    }
                    .collect { contact ->
                        mutableState.update { state ->
                            val updated =
                                (state.validatedContacts + contact).distinctBy {
                                    normalizePhoneNumber(it.phoneNo)
                                }
                            state.copy(validatedContacts = updated)
                        }
                    }
                mutableState.update { it.copy(isSyncing = false) }
                lastSyncedFingerprint = targetFingerprint
            }.onFailure { throwable ->
                mutableState.update { it.copy(isSyncing = false, errorMessage = throwable.message) }
            }
        }
    }

    fun inviteContact(contact: Contact) {
        // No-op: invite flow handled on the platform layer. Presenter intentionally left empty.
    }

    fun onContactSelected(contact: Contact) {
        scope.launch {
            if (contact.isRegistered) {
                mutableEvents.emit(ContactsEvent.OpenConversation(contact))
            }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun List<Contact>.fingerprint(): String =
        asSequence()
            .map { normalizePhoneNumber(it.phoneNo) }
            .filter { it.isNotBlank() }
            .sorted()
            .joinToString("#")
}

sealed interface ContactsEvent {
    data class ShareInvite(val contact: Contact, val message: String) : ContactsEvent

    data class OpenConversation(val contact: Contact) : ContactsEvent
}
