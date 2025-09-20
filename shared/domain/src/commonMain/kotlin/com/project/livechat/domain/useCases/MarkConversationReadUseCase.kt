package com.project.livechat.domain.useCases

import com.project.livechat.domain.repositories.IMessagesRepository

class MarkConversationReadUseCase(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(conversationId: String, lastReadAt: Long) {
        repository.markConversationAsRead(conversationId, lastReadAt)
    }
}
