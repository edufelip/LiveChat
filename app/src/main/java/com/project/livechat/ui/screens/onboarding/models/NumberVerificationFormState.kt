package com.project.livechat.ui.screens.onboarding.models

import com.google.firebase.auth.PhoneAuthProvider
import java.io.Serializable

data class NumberVerificationFormState(
    val phoneCode: String = "",
    val phoneNum: String = "",
    val phoneNumError: String? = null,
    val oneTimePass: String = "",
    val oneTimePassError: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val storedVerificationId: String = "",
    val token: PhoneAuthProvider.ForceResendingToken? = null
) : Serializable {
    val fullNumber: String
        get() = "+${phoneCode}${phoneNum}"
}