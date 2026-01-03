package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.useCases.DeleteAccountUseCase
import com.edufelip.livechat.domain.useCases.ObserveAccountProfileUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountDisplayNameUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountEmailUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountStatusMessageUseCase
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

class AccountPresenter(
    private val observeAccountProfile: ObserveAccountProfileUseCase,
    private val updateDisplayName: UpdateAccountDisplayNameUseCase,
    private val updateStatusMessage: UpdateAccountStatusMessageUseCase,
    private val updateEmail: UpdateAccountEmailUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(AccountUiState(isLoading = true))
    val state = mutableState.asStateFlow()
    val cState: CStateFlow<AccountUiState> = state.asCStateFlow()

    init {
        scope.launch {
            observeAccountProfile()
                .catch { throwable ->
                    mutableState.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: "Failed to load account")
                    }
                }
                .collectLatest { profile ->
                    mutableState.update {
                        it.copy(isLoading = false, profile = profile, errorMessage = null)
                    }
                }
        }
    }

    fun updateDisplayName(value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        updateProfile(
            update = { updateDisplayName(trimmed) },
            localUpdate = { it.copy(displayName = trimmed) },
        )
    }

    fun updateStatusMessage(value: String) {
        val trimmed = value.trim()
        updateProfile(
            update = { updateStatusMessage(trimmed) },
            localUpdate = { it.copy(statusMessage = trimmed) },
        )
    }

    fun updateEmail(value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        updateProfile(
            update = { updateEmail(trimmed) },
            localUpdate = { it.copy(email = trimmed) },
        )
    }

    fun requestDeleteAccount() {
        scope.launch {
            mutableState.update { it.copy(isDeleting = true, errorMessage = null) }
            runCatching { deleteAccount() }
                .onSuccess {
                    mutableState.update { it.copy(isDeleting = false, isDeleted = true) }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = throwable.message ?: "Unable to delete account",
                        )
                    }
                }
        }
    }

    fun acknowledgeDeletion() {
        mutableState.update { it.copy(isDeleted = false) }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }

    private fun updateProfile(
        update: suspend () -> Unit,
        localUpdate: (AccountProfile) -> AccountProfile,
    ) {
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { update() }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            profile = state.profile?.let(localUpdate),
                        )
                    }
                }
                .onFailure { throwable ->
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
