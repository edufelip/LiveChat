package com.project.livechat.domain.validators

import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors

class PhoneNumberValidator {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (android.util.Patterns.PHONE.matcher(phoneNumber).matches()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(OnBoardingValidationErrors.INVALID_NUMBER)
        }
    }
}