package com.edufelip.livechat.domain.models

import androidx.compose.runtime.Immutable
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession

@Immutable
data class PhoneAuthUiState(
    val isRequesting: Boolean = false,
    val isVerifying: Boolean = false,
    val countdownSeconds: Int = 0,
    val session: PhoneVerificationSession? = null,
    val error: PhoneAuthError? = null,
    val isVerificationCompleted: Boolean = false,
) {
    val hasActiveSession: Boolean get() = session != null
    val canResend: Boolean get() = countdownSeconds <= 0 && hasActiveSession
}
