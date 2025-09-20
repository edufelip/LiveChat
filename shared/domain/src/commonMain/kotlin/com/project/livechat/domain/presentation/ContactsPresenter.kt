package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.useCases.InviteContactUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsPresenter(
    private val getLocalContactsUseCase: GetLocalContactsUseCase,
    private val checkRegisteredContactsUseCase: CheckRegisteredContactsUseCase,
    private val inviteContactUseCase: InviteContactUseCase,
    private val scope: CoroutineScope = MainScope()
) {

    private val _uiState = MutableStateFlow(ContactsUiState(isLoading = true))
    val state = _uiState.asStateFlow()
    val cState: CStateFlow<ContactsUiState> = state.asCStateFlow()

    init {
        scope.launch {
            getLocalContactsUseCase()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                }
                .collectLatest { contacts ->
                    _uiState.update { state ->
                        state.copy(
                            localContacts = contacts,
                            validatedContacts = contacts,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun syncContacts(phoneContacts: List<Contact>) {
        scope.launch {
            val localContacts = _uiState.value.localContacts
            _uiState.update { it.copy(isSyncing = true, validatedContacts = emptyList(), errorMessage = null) }
            runCatching {
                checkRegisteredContactsUseCase(phoneContacts, localContacts)
            }.onSuccess { flow ->
                flow.catch { throwable ->
                    _uiState.update { it.copy(isSyncing = false, errorMessage = throwable.message) }
                }.collect { contact ->
                    _uiState.update { state ->
                        val updated = (state.validatedContacts + contact).distinctBy { it.phoneNo }
                        state.copy(validatedContacts = updated)
                    }
                }
                _uiState.update { it.copy(isSyncing = false) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSyncing = false, errorMessage = throwable.message) }
            }
        }
    }

    fun inviteContact(contact: Contact) {
        scope.launch {
            runCatching {
                inviteContactUseCase(contact)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }
}
