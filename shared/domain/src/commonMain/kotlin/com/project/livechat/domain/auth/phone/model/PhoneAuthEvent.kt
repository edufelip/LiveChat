package com.project.livechat.domain.auth.phone.model

sealed class PhoneAuthEvent {
    data object Loading : PhoneAuthEvent()
    data class CodeSent(val session: PhoneVerificationSession) : PhoneAuthEvent()
    data class VerificationCompleted(val session: PhoneVerificationSession) : PhoneAuthEvent()
    data class Error(val error: PhoneAuthError) : PhoneAuthEvent()
}
