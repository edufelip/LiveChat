package com.project.livechat.ui.screens.onboarding.models

data class NumberVerificationFormState(
    val phoneCode: String = "",
    val phoneNum: String = "",
    val phoneError: String? = null,

    val currentPage: Int = 0
)