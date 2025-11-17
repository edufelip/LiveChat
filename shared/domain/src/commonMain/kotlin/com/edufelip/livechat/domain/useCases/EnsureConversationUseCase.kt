package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.repositories.IMessagesRepository

class EnsureConversationUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        peer: ConversationPeer? = null,
    ) {
        if (conversationId.isBlank()) return
        messagesRepository.ensureConversation(conversationId, peer)
    }
}
