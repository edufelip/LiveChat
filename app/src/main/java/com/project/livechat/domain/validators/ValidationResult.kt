package com.project.livechat.domain.validators

import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors

sealed interface ValidationResult {
    object Idle: ValidationResult
    object Success: ValidationResult
    data class Error(var errorType: OnBoardingValidationErrors): ValidationResult
}