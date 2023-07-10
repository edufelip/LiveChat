package com.project.livechat.domain.validators

import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors
import javax.inject.Inject

class PhoneNumberValidator @Inject constructor() {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (android.util.Patterns.PHONE.matcher(phoneNumber).matches()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(OnBoardingValidationErrors.INVALID_NUMBER)
        }
    }
}