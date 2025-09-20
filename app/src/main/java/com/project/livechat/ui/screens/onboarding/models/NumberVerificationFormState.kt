package com.project.livechat.ui.screens.onboarding.models

import java.io.Serializable
import java.util.Locale
import com.google.firebase.auth.PhoneAuthProvider

data class NumberVerificationFormState(
    val phoneCode: String = "1",
    val phoneNum: String = "",
    val phoneNumError: String? = null,
    val oneTimePass: String = "",
    val oneTimePassError: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val countryIso: String = Locale.getDefault().country.ifEmpty { "US" },
    val storedVerificationId: String = "",
    val token: PhoneAuthProvider.ForceResendingToken? = null
) : Serializable {
    val fullNumber: String
        get() = "+${phoneCode}${phoneNum}"
}
