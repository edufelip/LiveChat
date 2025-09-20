package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationUseCase(
    private val messagesRepository: IMessagesRepository
) {
    operator fun invoke(conversationId: String, pageSize: Int = IMessagesRepository.DEFAULT_PAGE_SIZE): Flow<List<Message>> {
        return messagesRepository.observeConversation(conversationId, pageSize)
    }
}
