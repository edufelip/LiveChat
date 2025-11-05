package com.project.livechat.domain.useCases.phone

import com.project.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.project.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.project.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.project.livechat.domain.repositories.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow

class ResendPhoneVerificationUseCase(
    private val repository: IPhoneAuthRepository,
) {
    operator fun invoke(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> =
        repository.resendVerification(session, presentationContext)
}
