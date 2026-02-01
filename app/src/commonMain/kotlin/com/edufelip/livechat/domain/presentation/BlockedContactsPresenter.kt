package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.domain.models.BlockedContactsUiState
import com.edufelip.livechat.domain.useCases.BlockContactUseCase
import com.edufelip.livechat.domain.useCases.ObserveBlockedContactsUseCase
import com.edufelip.livechat.domain.useCases.UnblockContactUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlockedContactsPresenter(
    private val observeBlockedContacts: ObserveBlockedContactsUseCase,
    private val blockContact: BlockContactUseCase,
    private val unblockContact: UnblockContactUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(BlockedContactsUiState())
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<BlockedContactsUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeBlockedContacts()
                .catch { throwable ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load blocked contacts",
                        )
                    }
                }.collectLatest { contacts ->
                    mutableState.update {
                        it.copy(isLoading = false, contacts = contacts, errorMessage = null)
                    }
                }
        }
    }

    fun blockContact(contact: BlockedContact) {
        updateContacts(
            update = { blockContact(contact) },
            localUpdate = { current ->
                if (current.any { it.userId == contact.userId }) current else current + contact
            },
        )
    }

    fun unblockContact(userId: String) {
        updateContacts(
            update = { unblockContact(userId) },
            localUpdate = { current -> current.filterNot { it.userId == userId } },
        )
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun updateContacts(
        update: suspend () -> Unit,
        localUpdate: (List<BlockedContact>) -> List<BlockedContact>,
    ) {
        if (mutableState.value.isUpdating) return
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { update() }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            contacts = localUpdate(state.contacts),
                        )
                    }
                }.onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Update failed",
                        )
                    }
                }
        }
    }
}
