package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IMessagesRepository

class EnsureUserInboxUseCase(
    private val messagesRepository: IMessagesRepository,
    private val sessionProvider: UserSessionProvider,
) {
    suspend operator fun invoke() {
        val userId = sessionProvider.currentUserId() ?: return
        messagesRepository.ensureConversation(conversationId = userId, peer = null)
    }
}
