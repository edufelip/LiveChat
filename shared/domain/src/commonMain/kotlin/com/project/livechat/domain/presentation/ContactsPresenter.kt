package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.models.InviteChannel
import com.project.livechat.domain.models.InviteHistoryItem
import com.project.livechat.domain.repositories.IInviteHistoryRepository
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.useCases.InviteContactUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import com.project.livechat.domain.utils.currentEpochMillis
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
    private val inviteHistoryRepository: IInviteHistoryRepository,
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

        scope.launch {
            inviteHistoryRepository.history.collectLatest { history ->
                mutableState.update { it.copy(inviteHistory = history) }
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

    fun inviteContact(
        contact: Contact,
        channel: InviteChannel,
    ) {
        scope.launch {
            runCatching {
                val result = inviteContactUseCase(contact, channel)
                inviteHistoryRepository.record(
                    InviteHistoryItem(
                        contact = contact,
                        channel = channel,
                        timestamp = currentEpochMillis(),
                    ),
                )
                mutableEvents.tryEmit(ContactsEvent.ShareInvite(contact, channel, result.message))
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
    data class ShareInvite(val contact: Contact, val channel: InviteChannel, val message: String) : ContactsEvent

    data class OpenConversation(val contact: Contact) : ContactsEvent
}
