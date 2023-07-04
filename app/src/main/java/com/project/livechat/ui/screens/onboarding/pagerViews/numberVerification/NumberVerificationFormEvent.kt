package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

sealed interface NumberVerificationFormEvent {
    data class PhoneCodeChanged(val code: String): NumberVerificationFormEvent
    data class PhoneNumberChanged(val number: String): NumberVerificationFormEvent
    data class OneTimePassChanged(val password: String): NumberVerificationFormEvent
    object Submit: NumberVerificationFormEvent
}