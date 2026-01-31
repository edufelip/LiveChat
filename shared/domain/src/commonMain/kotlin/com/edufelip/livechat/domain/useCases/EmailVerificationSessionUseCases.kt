package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.EmailVerificationSession
import com.edufelip.livechat.domain.repositories.IEmailVerificationSessionRepository

class GetEmailVerificationSessionUseCase(
    private val repository: IEmailVerificationSessionRepository,
) {
    suspend operator fun invoke(userId: String): EmailVerificationSession? = repository.loadSession(userId)
}

class SaveEmailVerificationSessionUseCase(
    private val repository: IEmailVerificationSessionRepository,
) {
    suspend operator fun invoke(
        userId: String,
        session: EmailVerificationSession,
    ) {
        repository.saveSession(userId, session)
    }
}

class ClearEmailVerificationSessionUseCase(
    private val repository: IEmailVerificationSessionRepository,
) {
    suspend operator fun invoke(userId: String) {
        repository.clearSession(userId)
    }
}
