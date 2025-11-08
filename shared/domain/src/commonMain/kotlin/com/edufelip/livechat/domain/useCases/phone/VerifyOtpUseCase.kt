package com.edufelip.livechat.domain.useCases.phone

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository

class VerifyOtpUseCase(
    private val repository: IPhoneAuthRepository,
) {
    suspend operator fun invoke(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult = repository.verifyCode(session, code)
}
