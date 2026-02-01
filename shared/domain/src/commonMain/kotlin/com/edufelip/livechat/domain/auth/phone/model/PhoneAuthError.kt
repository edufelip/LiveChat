package com.edufelip.livechat.domain.auth.phone.model

sealed class PhoneAuthError {
    data object InvalidPhoneNumber : PhoneAuthError()

    data object InvalidVerificationCode : PhoneAuthError()

    data object TooManyRequests : PhoneAuthError()

    data object QuotaExceeded : PhoneAuthError()

    data object CodeExpired : PhoneAuthError()

    data object NetworkError : PhoneAuthError()

    data object ResendNotAvailable : PhoneAuthError()

    data class Configuration(
        val message: String?,
    ) : PhoneAuthError()

    data class Unknown(
        val message: String?,
    ) : PhoneAuthError()
}
