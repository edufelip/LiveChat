package com.edufelip.livechat.domain.models

data class EmailVerificationSession(
    val email: String,
    val step: EmailVerificationStep,
    val resendAvailableAtEpochMillis: Long,
    val previousEmail: String? = null,
)

enum class EmailVerificationStep {
    Entry,
    AwaitVerification,
    ;

    companion object {
        fun fromRaw(value: String?): EmailVerificationStep? = entries.firstOrNull { it.name == value }
    }
}
