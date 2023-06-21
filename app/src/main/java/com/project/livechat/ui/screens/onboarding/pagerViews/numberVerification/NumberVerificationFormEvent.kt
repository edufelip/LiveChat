package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

sealed class NumberVerificationFormEvent {
    data class PhoneCodeChanged(val code: String): NumberVerificationFormEvent()
    data class PhoneNumberChanged(val number: String): NumberVerificationFormEvent()
    object Submit: NumberVerificationFormEvent()
}