package com.edufelip.livechat.domain.useCases.phone

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.useCases.EnsureUserInboxUseCase

class VerifyOtpUseCase(
    private val repository: IPhoneAuthRepository,
    private val ensureUserInbox: EnsureUserInboxUseCase,
) {
    suspend operator fun invoke(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult {
        val result = repository.verifyCode(session, code)

        // Create user inbox immediately after successful authentication
        if (result is PhoneAuthResult.Success) {
            runCatching {
                ensureUserInbox()
            }.onFailure { throwable ->
                println("COMMCHECK: Failed to create user inbox after phone auth: ${throwable.message}")
            }
        }

        return result
    }
}
