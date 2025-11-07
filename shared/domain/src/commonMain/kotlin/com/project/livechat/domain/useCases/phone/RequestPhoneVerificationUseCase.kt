package com.project.livechat.domain.useCases.phone

import com.project.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.project.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.project.livechat.domain.auth.phone.model.PhoneNumber
import com.project.livechat.domain.repositories.IPhoneAuthRepository
import kotlinx.coroutines.flow.Flow

class RequestPhoneVerificationUseCase(
    private val repository: IPhoneAuthRepository,
) {
    operator fun invoke(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = repository.requestVerification(phoneNumber, presentationContext)
}
