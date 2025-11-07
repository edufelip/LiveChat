package com.project.livechat.domain.useCases.phone

import com.project.livechat.domain.auth.phone.model.PhoneAuthResult
import com.project.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.project.livechat.domain.repositories.IPhoneAuthRepository

class VerifyOtpUseCase(
    private val repository: IPhoneAuthRepository,
) {
    suspend operator fun invoke(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult = repository.verifyCode(session, code)
}
