package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.InviteContactUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
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
    private val inviteContactUseCase: InviteContactUseCase,
    private val scope: CoroutineScope = MainScope(),
) {
    private val mutableState = MutableStateFlow(ContactsUiState(isLoading = true))
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<ContactsUiState> = state.asCStateFlow()
    private val mutableEvents = MutableSharedFlow<ContactsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ContactsEvent> = mutableEvents

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

    fun syncContacts(phoneContacts: List<Contact>) {
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
                            val updated = (state.validatedContacts + contact).distinctBy { it.phoneNo }
                            state.copy(validatedContacts = updated)
                        }
                    }
                mutableState.update { it.copy(isSyncing = false) }
            }.onFailure { throwable ->
                mutableState.update { it.copy(isSyncing = false, errorMessage = throwable.message) }
            }
        }
    }

    fun inviteContact(contact: Contact) {
        scope.launch {
            runCatching {
                val result = inviteContactUseCase(contact)
                mutableEvents.tryEmit(ContactsEvent.ShareInvite(contact, result.message))
            }.onFailure { throwable ->
                mutableState.update { it.copy(errorMessage = throwable.message) }
            }
        }
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
}

sealed interface ContactsEvent {
    data class ShareInvite(val contact: Contact, val message: String) : ContactsEvent

    data class OpenConversation(val contact: Contact) : ContactsEvent
}
