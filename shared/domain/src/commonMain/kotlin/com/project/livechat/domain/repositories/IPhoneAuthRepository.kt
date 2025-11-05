package com.project.livechat.domain.repositories

import com.project.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.project.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.project.livechat.domain.auth.phone.model.PhoneAuthResult
import com.project.livechat.domain.auth.phone.model.PhoneNumber
import com.project.livechat.domain.auth.phone.model.PhoneVerificationSession
import kotlinx.coroutines.flow.Flow

interface IPhoneAuthRepository {
    fun requestVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent>

    suspend fun verifyCode(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult

    fun resendVerification(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent>

    fun clearActiveSession()
}
