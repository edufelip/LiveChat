package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.useCases.DeleteAccountUseCase
import com.edufelip.livechat.domain.useCases.ObserveAccountProfileUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountDisplayNameUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountEmailUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountPhotoUseCase
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
    private val updateDisplayNameUseCase: UpdateAccountDisplayNameUseCase,
    private val updateStatusMessageUseCase: UpdateAccountStatusMessageUseCase,
    private val updateEmailUseCase: UpdateAccountEmailUseCase,
    private val updatePhotoUseCase: UpdateAccountPhotoUseCase,
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
                }.collectLatest { profile ->
                    mutableState.update {
                        it.copy(isLoading = false, profile = profile, errorMessage = null)
                    }
                }
        }
    }

    fun updateDisplayName(value: String) {
        val stackTrace =
            Exception()
                .stackTraceToString()
                .lines()
                .take(5)
                .joinToString("\n")
        println("🎨 AccountPresenter.updateDisplayName called: '$value'")
        println("  📍 Call stack:\n$stackTrace")
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            println("  ⚠️ Empty value, skipping update")
            return
        }
        updateProfile(
            update = { updateDisplayNameUseCase(trimmed) },
            localUpdate = { it.copy(displayName = trimmed) },
        )
    }

    fun updateStatusMessage(value: String) {
        println("🎨 AccountPresenter.updateStatusMessage called: '$value'")
        val trimmed = value.trim()
        updateProfile(
            update = { updateStatusMessageUseCase(trimmed) },
            localUpdate = { it.copy(statusMessage = trimmed) },
        )
    }

    fun updateEmail(value: String) {
        println("🎨 AccountPresenter.updateEmail called: '$value'")
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            println("  ⚠️ Empty value, skipping update")
            return
        }
        updateProfile(
            update = { updateEmailUseCase(trimmed) },
            localUpdate = { it.copy(email = trimmed) },
        )
    }

    fun updatePhoto(localPath: String) {
        val trimmed = localPath.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { updatePhotoUseCase(trimmed) }
                .onSuccess { photoUrl ->
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            profile = state.profile?.copy(photoUrl = photoUrl, photoLocalPath = null),
                        )
                    }
                }.onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Unable to update photo",
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
                }.onFailure { throwable ->
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
        // Prevent concurrent updates
        if (mutableState.value.isUpdating) {
            println("  ⚠️ Update already in progress, ignoring duplicate call")
            return
        }

        scope.launch {
            println("  🔄 Starting profile update...")
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { update() }
                .onSuccess {
                    println("  ✅ Profile update succeeded!")
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            profile = state.profile?.let(localUpdate),
                        )
                    }
                }.onFailure { throwable ->
                    println("  ❌ Profile update failed: ${throwable.message}")
                    throwable.printStackTrace()
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
