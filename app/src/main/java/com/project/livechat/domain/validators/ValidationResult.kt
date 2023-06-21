package com.project.livechat.domain.validators

import com.project.livechat.ui.screens.onboarding.OnBoardingErrors

sealed class ValidationResult {
    object Idle: ValidationResult()
    object Success: ValidationResult()
    data class Error(var errorType: OnBoardingErrors): ValidationResult()

    fun isIdle(): Boolean = this is Idle
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}