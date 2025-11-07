package com.project.livechat.data.repositories

import com.project.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.project.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.project.livechat.domain.auth.phone.model.PhoneAuthResult
import com.project.livechat.domain.auth.phone.model.PhoneNumber
import com.project.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.project.livechat.domain.repositories.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UnsupportedPhoneAuthRepository : IPhoneAuthRepository {
    override fun requestVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = flow { throw UnsupportedOperationException("Phone auth not supported on this platform") }

    override suspend fun verifyCode(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult = throw UnsupportedOperationException("Phone auth not supported on this platform")

    override fun resendVerification(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = flow { throw UnsupportedOperationException("Phone auth not supported on this platform") }

    override fun clearActiveSession() {
        // no-op
    }
}
