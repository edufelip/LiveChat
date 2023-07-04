package com.project.livechat.ui.screens.onboarding.models

import com.google.firebase.auth.PhoneAuthProvider

data class NumberVerificationFormState(
    val phoneCode: String = "",
    val phoneNum: String = "",
    val phoneError: String? = null,
    val oneTimePass: String = "",
    val oneTimePassEError: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 3.apply { this.minus(1) },

    val storedVerificationId: String? = null,
    val token: PhoneAuthProvider.ForceResendingToken? = null
) {
    val fullNumber: String
        get() = "+${phoneCode}${phoneNum}"
}