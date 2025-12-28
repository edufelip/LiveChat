package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IMessagesRepository

class DeleteMessageLocalUseCase(
    private val repository: IMessagesRepository,
) {
    suspend operator fun invoke(messageId: String) {
        repository.deleteMessageLocal(messageId)
    }
}
