package com.project.livechat.domain.validators

sealed class ValidationResult {
    object Idle: ValidationResult()
    object Success: ValidationResult()
    data class Error(var message: String): ValidationResult()

    fun isIdle(): Boolean = this is Idle
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}