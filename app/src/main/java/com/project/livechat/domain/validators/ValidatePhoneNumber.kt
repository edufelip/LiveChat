package com.project.livechat.domain.validators

import android.telephony.PhoneNumberUtils

class ValidatePhoneNumber {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("This number is not valid")
        }
    }
}