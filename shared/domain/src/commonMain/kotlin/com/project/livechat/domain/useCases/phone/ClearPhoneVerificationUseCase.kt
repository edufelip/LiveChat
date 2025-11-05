package com.project.livechat.domain.useCases.phone

import com.project.livechat.domain.repositories.IPhoneAuthRepository

class ClearPhoneVerificationUseCase(
    private val repository: IPhoneAuthRepository,
) {
    operator fun invoke() {
        repository.clearActiveSession()
    }
}
