package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.repositories.IAccountRepository
import kotlinx.coroutines.flow.Flow

class ObserveAccountProfileUseCase(
    private val repository: IAccountRepository,
) {
    operator fun invoke(): Flow<AccountProfile?> = repository.observeAccountProfile()
}

class UpdateAccountDisplayNameUseCase(
    private val repository: IAccountRepository,
) {
    suspend operator fun invoke(displayName: String) {
        repository.updateDisplayName(displayName)
    }
}

class UpdateAccountStatusMessageUseCase(
    private val repository: IAccountRepository,
) {
    suspend operator fun invoke(statusMessage: String) {
        repository.updateStatusMessage(statusMessage)
    }
}

class UpdateAccountEmailUseCase(
    private val repository: IAccountRepository,
) {
    suspend operator fun invoke(email: String) {
        repository.updateEmail(email)
    }
}

class DeleteAccountUseCase(
    private val repository: IAccountRepository,
) {
    suspend operator fun invoke() {
        repository.deleteAccount()
    }
}
