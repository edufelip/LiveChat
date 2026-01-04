package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.models.EmailUpdateState
import com.edufelip.livechat.domain.useCases.CheckEmailUpdatedUseCase
import com.edufelip.livechat.domain.useCases.DeleteAccountUseCase
import com.edufelip.livechat.domain.useCases.ObserveAccountProfileUseCase
import com.edufelip.livechat.domain.useCases.SendEmailVerificationUseCase
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
    private val sendEmailVerification: SendEmailVerificationUseCase,
    private val checkEmailUpdated: CheckEmailUpdatedUseCase,
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

    fun sendEmailVerification(value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            mutableState.update {
                it.copy(
                    isUpdating = true,
                    errorMessage = null,
                    emailUpdateState = EmailUpdateState.Sending(trimmed),
                )
            }
            runCatching { sendEmailVerification(trimmed) }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Sent(trimmed),
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Idle,
                            errorMessage = throwable.message ?: "Unable to send verification email",
                        )
                    }
                }
        }
    }

    fun confirmEmailUpdate(value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            mutableState.update {
                it.copy(
                    isUpdating = true,
                    errorMessage = null,
                    emailUpdateState = EmailUpdateState.Verifying(trimmed),
                )
            }
            runCatching {
                val verified = checkEmailUpdated(trimmed)
                if (!verified) {
                    error("Email not verified yet. Please confirm the link in your inbox.")
                }
                updateEmail(trimmed)
            }
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Verified(trimmed),
                            profile = state.profile?.copy(email = trimmed),
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Idle,
                            errorMessage = throwable.message ?: "Unable to verify email",
                        )
                    }
                }
        }
    }

    fun requestDeleteAccount() {
        scope.launch {
            mutableState.update { it.copy(isDeleting = true, errorMessage = null, requiresReauth = false) }
            runCatching { deleteAccount() }
                .onSuccess {
                    mutableState.update { it.copy(isDeleting = false, isDeleted = true) }
                }
                .onFailure { throwable ->
                    val requiresReauth = throwable is com.edufelip.livechat.domain.errors.RecentLoginRequiredException
                    mutableState.update {
                        it.copy(
                            isDeleting = false,
                            requiresReauth = requiresReauth,
                            errorMessage =
                                if (requiresReauth) {
                                    null
                                } else {
                                    throwable.message ?: "Unable to delete account"
                                },
                        )
                    }
                }
        }
    }

    fun acknowledgeDeletion() {
        mutableState.update { it.copy(isDeleted = false) }
    }

    fun clearEmailUpdateState() {
        mutableState.update { it.copy(emailUpdateState = EmailUpdateState.Idle) }
    }

    fun clearReauthRequirement() {
        mutableState.update { it.copy(requiresReauth = false) }
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
