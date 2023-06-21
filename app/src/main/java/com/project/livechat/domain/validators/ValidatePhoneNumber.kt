package com.project.livechat.domain.validators

import android.telephony.PhoneNumberUtils
import com.project.livechat.ui.screens.onboarding.OnBoardingErrors

class ValidatePhoneNumber {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(OnBoardingErrors.INVALID_NUMBER) // "This number is not valid"
        }
    }
}