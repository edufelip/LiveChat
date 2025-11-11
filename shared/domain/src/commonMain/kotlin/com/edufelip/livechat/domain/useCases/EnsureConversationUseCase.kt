package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IMessagesRepository

class EnsureConversationUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(conversationId: String) {
        if (conversationId.isBlank()) return
        messagesRepository.ensureConversation(conversationId)
    }
}
