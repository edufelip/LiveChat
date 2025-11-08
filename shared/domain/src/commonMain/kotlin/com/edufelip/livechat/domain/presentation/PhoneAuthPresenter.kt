package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.models.PhoneAuthUiState
import com.edufelip.livechat.domain.useCases.phone.ClearPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.RequestPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.ResendPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.VerifyOtpUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneAuthPresenter(
    private val requestVerification: RequestPhoneVerificationUseCase,
    private val verifyOtp: VerifyOtpUseCase,
    private val resendVerification: ResendPhoneVerificationUseCase,
    private val clearVerification: ClearPhoneVerificationUseCase,
    private val scope: CoroutineScope = MainScope(),
) {
    private val _uiState = MutableStateFlow(PhoneAuthUiState())
    val uiState = _uiState.asStateFlow()
    val cState: CStateFlow<PhoneAuthUiState> = uiState.asCStateFlow()

    private var countdownJob: Job? = null
    private var verificationJob: Job? = null

    fun startVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ) {
        if (_uiState.value.isRequesting) return
        verificationJob?.cancel()
        verificationJob =
            scope.launch {
                requestVerification(phoneNumber, presentationContext)
                    .collectLatest { event ->
                        when (event) {
                            PhoneAuthEvent.Loading ->
                                _uiState.update {
                                    it.copy(
                                        isRequesting = true,
                                        error = null,
                                        isVerificationCompleted = false,
                                    )
                                }

                            is PhoneAuthEvent.CodeSent -> {
                                _uiState.update {
                                    it.copy(
                                        isRequesting = false,
                                        session = event.session,
                                        countdownSeconds = DEFAULT_COUNTDOWN_SECONDS,
                                        error = null,
                                        isVerificationCompleted = false,
                                    )
                                }
                                restartCountdown()
                            }

                            is PhoneAuthEvent.VerificationCompleted -> {
                                _uiState.update {
                                    it.copy(
                                        isRequesting = false,
                                        isVerifying = false,
                                        session = event.session,
                                        isVerificationCompleted = true,
                                        error = null,
                                    )
                                }
                                stopCountdown()
                            }

                            is PhoneAuthEvent.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isRequesting = false,
                                        isVerifying = false,
                                        error = event.error,
                                    )
                                }
                                stopCountdownIfNoSession()
                            }
                        }
                    }
            }
    }

    fun verifyCode(code: String) {
        val session = _uiState.value.session ?: return
        val sanitized = code.trim()
        if (sanitized.length != OTP_LENGTH || sanitized.any { !it.isDigit() }) {
            _uiState.update { it.copy(error = PhoneAuthError.InvalidVerificationCode) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            when (val result = verifyOtp(session, sanitized)) {
                PhoneAuthResult.Success -> {
                    _uiState.update {
                        it.copy(isVerifying = false, isVerificationCompleted = true)
                    }
                    stopCountdown()
                }

                is PhoneAuthResult.Failure -> {
                    _uiState.update {
                        it.copy(isVerifying = false, error = result.error)
                    }
                }
            }
        }
    }

    fun resendCode(presentationContext: PhoneAuthPresentationContext) {
        val session = _uiState.value.session ?: return
        if (!_uiState.value.canResend) return
        verificationJob?.cancel()
        verificationJob =
            scope.launch {
                resendVerification(session, presentationContext)
                    .collectLatest { event ->
                        when (event) {
                            PhoneAuthEvent.Loading ->
                                _uiState.update {
                                    it.copy(
                                        isRequesting = true,
                                        error = null,
                                        isVerificationCompleted = false,
                                    )
                                }

                            is PhoneAuthEvent.CodeSent -> {
                                _uiState.update {
                                    it.copy(
                                        isRequesting = false,
                                        session = event.session,
                                        countdownSeconds = DEFAULT_COUNTDOWN_SECONDS,
                                        error = null,
                                    )
                                }
                                restartCountdown()
                            }

                            is PhoneAuthEvent.Error ->
                                _uiState.update {
                                    it.copy(isRequesting = false, error = event.error)
                                }

                            is PhoneAuthEvent.VerificationCompleted -> {
                                _uiState.update {
                                    it.copy(
                                        isRequesting = false,
                                        isVerificationCompleted = true,
                                        session = event.session,
                                    )
                                }
                                stopCountdown()
                            }
                        }
                    }
            }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun close() {
        stopCountdown()
        clearVerification()
        verificationJob?.cancel()
        verificationJob = null
        scope.cancel()
    }

    private fun restartCountdown() {
        countdownJob?.cancel()
        countdownJob =
            scope.launch {
                for (second in DEFAULT_COUNTDOWN_SECONDS downTo 0) {
                    _uiState.update { it.copy(countdownSeconds = second) }
                    delay(1_000)
                }
            }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.update { it.copy(countdownSeconds = 0) }
    }

    private fun stopCountdownIfNoSession() {
        if (_uiState.value.session == null) {
            stopCountdown()
        }
    }

    private companion object {
        const val DEFAULT_COUNTDOWN_SECONDS = 60
        const val OTP_LENGTH = 6
    }
}
