package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    operator fun invoke(
        conversationId: String,
        pageSize: Int = IMessagesRepository.DEFAULT_PAGE_SIZE,
    ): Flow<List<Message>> = messagesRepository.observeConversation(conversationId, pageSize)

    fun observeAll(): Flow<List<Message>> = messagesRepository.observeAllIncomingMessages()
}
