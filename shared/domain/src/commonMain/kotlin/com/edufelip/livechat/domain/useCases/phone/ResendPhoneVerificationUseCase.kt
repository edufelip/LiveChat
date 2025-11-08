package com.edufelip.livechat.domain.useCases.phone

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow

class ResendPhoneVerificationUseCase(
    private val repository: IPhoneAuthRepository,
) {
    operator fun invoke(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = repository.resendVerification(session, presentationContext)
}
