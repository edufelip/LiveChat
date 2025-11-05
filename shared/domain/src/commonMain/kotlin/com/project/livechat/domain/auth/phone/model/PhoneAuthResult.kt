package com.project.livechat.domain.auth.phone.model

sealed class PhoneAuthResult {
    data object Success : PhoneAuthResult()
    data class Failure(val error: PhoneAuthError) : PhoneAuthResult()
}
