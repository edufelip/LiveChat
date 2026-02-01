package com.edufelip.livechat.domain.validation

class PhoneNumberValidator {
    operator fun invoke(phoneNumber: String): ValidationResult =
        if (isPhoneNumberValid(phoneNumber)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(ValidationError.InvalidPhoneNumber)
        }
}
