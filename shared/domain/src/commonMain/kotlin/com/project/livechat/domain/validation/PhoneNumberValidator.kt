package com.project.livechat.domain.validation

class PhoneNumberValidator {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (isPhoneNumberValid(phoneNumber)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(ValidationError.InvalidPhoneNumber)
        }
    }
}
