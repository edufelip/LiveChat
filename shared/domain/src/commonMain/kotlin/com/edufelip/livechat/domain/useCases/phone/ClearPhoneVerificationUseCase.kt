package com.edufelip.livechat.domain.useCases.phone

import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository

class ClearPhoneVerificationUseCase(
    private val repository: IPhoneAuthRepository,
) {
    operator fun invoke() {
        repository.clearActiveSession()
    }
}
