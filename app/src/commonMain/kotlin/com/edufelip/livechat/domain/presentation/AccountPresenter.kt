package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.models.EmailVerificationSession
import com.edufelip.livechat.domain.models.EmailVerificationStep
import com.edufelip.livechat.domain.models.EmailUpdateState
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.useCases.CheckEmailUpdatedUseCase
import com.edufelip.livechat.domain.useCases.ClearEmailVerificationSessionUseCase
import com.edufelip.livechat.domain.useCases.DeleteAccountUseCase
import com.edufelip.livechat.domain.useCases.GetEmailVerificationSessionUseCase
import com.edufelip.livechat.domain.useCases.ObserveAccountProfileUseCase
import com.edufelip.livechat.domain.useCases.SaveEmailVerificationSessionUseCase
import com.edufelip.livechat.domain.useCases.SendEmailVerificationUseCase
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AccountPresenter(
    private val observeAccountProfile: ObserveAccountProfileUseCase,
    private val updateDisplayName: UpdateAccountDisplayNameUseCase,
    private val updateStatusMessage: UpdateAccountStatusMessageUseCase,
    private val updateEmail: UpdateAccountEmailUseCase,
    private val updatePhoto: UpdateAccountPhotoUseCase,
    private val sendEmailVerification: SendEmailVerificationUseCase,
    private val getEmailVerificationSessionUseCase: GetEmailVerificationSessionUseCase,
    private val saveEmailVerificationSessionUseCase: SaveEmailVerificationSessionUseCase,
    private val clearEmailVerificationSessionUseCase: ClearEmailVerificationSessionUseCase,
    private val checkEmailUpdated: CheckEmailUpdatedUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val sessionProvider: UserSessionProvider,
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
                    val storedSession = mutableState.value.emailVerificationSession
                    val profileEmail = profile?.email?.trim()
                    val sessionEmail = storedSession?.email?.trim()
                    val previousEmail = storedSession?.previousEmail?.trim()
                    val shouldClearSession =
                        !profileEmail.isNullOrBlank() &&
                            !sessionEmail.isNullOrBlank() &&
                            profileEmail == sessionEmail &&
                            !previousEmail.isNullOrBlank() &&
                            previousEmail != profileEmail
                    if (shouldClearSession) {
                        clearStoredEmailVerificationSession(sessionProvider.currentUserId())
                        mutableState.update { it.copy(emailVerificationSession = null) }
                    }
                }
        }
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                if (session == null) {
                    mutableState.update { it.copy(emailVerificationSession = null) }
                    return@collectLatest
                }
                val storedSession =
                    runCatching { getEmailVerificationSessionUseCase(session.userId) }
                        .getOrNull()
                mutableState.update { it.copy(emailVerificationSession = storedSession) }
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

    fun updatePhoto(localPath: String) {
        val trimmed = localPath.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            mutableState.update { it.copy(isUpdating = true, errorMessage = null) }
            runCatching { updatePhoto(trimmed) }
                .onSuccess { photoUrl ->
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            profile = state.profile?.copy(photoUrl = photoUrl),
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Unable to update photo",
                        )
                    }
                }
        }
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
                    val session = buildEmailVerificationSession(trimmed)
                    persistEmailVerificationSession(session)
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Sent(trimmed),
                            emailVerificationSession = session,
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
                    clearStoredEmailVerificationSession(sessionProvider.currentUserId())
                    mutableState.update { state ->
                        state.copy(
                            isUpdating = false,
                            emailUpdateState = EmailUpdateState.Verified(trimmed),
                            profile = state.profile?.copy(email = trimmed),
                            emailVerificationSession = null,
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

    fun clearEmailVerificationSession() {
        scope.launch {
            clearStoredEmailVerificationSession(sessionProvider.currentUserId())
            mutableState.update {
                it.copy(
                    emailUpdateState = EmailUpdateState.Idle,
                    emailVerificationSession = null,
                )
            }
        }
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

    @OptIn(ExperimentalTime::class)
    private fun buildEmailVerificationSession(email: String): EmailVerificationSession {
        val now = Clock.System.now().toEpochMilliseconds()
        val previousEmail = mutableState.value.profile?.email?.trim()
        return EmailVerificationSession(
            email = email,
            step = EmailVerificationStep.AwaitVerification,
            resendAvailableAtEpochMillis = now + EMAIL_RESEND_DELAY_SECONDS * 1_000L,
            previousEmail = previousEmail,
        )
    }

    private suspend fun persistEmailVerificationSession(session: EmailVerificationSession) {
        val userId = sessionProvider.currentUserId()?.takeIf { it.isNotBlank() } ?: return
        runCatching { saveEmailVerificationSessionUseCase(userId, session) }
    }

    private suspend fun clearStoredEmailVerificationSession(userId: String?) {
        val resolvedUserId = userId?.takeIf { it.isNotBlank() } ?: return
        runCatching { clearEmailVerificationSessionUseCase(resolvedUserId) }
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

private const val EMAIL_RESEND_DELAY_SECONDS = 300
