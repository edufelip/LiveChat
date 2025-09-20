package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.repositories.IMessagesRepository

class SyncConversationUseCase(
    private val messagesRepository: IMessagesRepository
) {
    suspend operator fun invoke(conversationId: String, sinceEpochMillis: Long?): List<Message> {
        return messagesRepository.syncConversation(conversationId, sinceEpochMillis)
    }
}
