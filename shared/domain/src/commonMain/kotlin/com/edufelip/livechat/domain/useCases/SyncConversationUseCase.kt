package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.repositories.IMessagesRepository

class SyncConversationUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message> {
        return messagesRepository.syncConversation(conversationId, sinceEpochMillis)
    }
}
