package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.domain.models.EmailVerificationSession
import com.edufelip.livechat.domain.models.EmailVerificationStep
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object EmailVerificationSessionCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(session: EmailVerificationSession): String = json.encodeToString(EmailVerificationSessionPayload.fromDomain(session))

    fun decode(raw: String): EmailVerificationSession? =
        runCatching { json.decodeFromString<EmailVerificationSessionPayload>(raw).toDomain() }
            .getOrNull()
}

@Serializable
internal data class EmailVerificationSessionPayload(
    val email: String,
    val step: String,
    val resendAvailableAtEpochMillis: Long,
    val previousEmail: String? = null,
) {
    fun toDomain(): EmailVerificationSession? {
        val parsedStep = EmailVerificationStep.fromRaw(step) ?: return null
        if (email.isBlank()) return null
        return EmailVerificationSession(
            email = email,
            step = parsedStep,
            resendAvailableAtEpochMillis = resendAvailableAtEpochMillis,
            previousEmail = previousEmail,
        )
    }

    companion object {
        fun fromDomain(session: EmailVerificationSession): EmailVerificationSessionPayload =
            EmailVerificationSessionPayload(
                email = session.email,
                step = session.step.name,
                resendAvailableAtEpochMillis = session.resendAvailableAtEpochMillis,
                previousEmail = session.previousEmail,
            )
    }
}
