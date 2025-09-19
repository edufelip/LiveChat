package com.project.livechat.domain.validation

sealed class ValidationResult {
    data object Idle : ValidationResult()
    data object Success : ValidationResult()
    data class Error(val type: ValidationError) : ValidationResult()
}

enum class ValidationError {
    InvalidPhoneNumber
}
