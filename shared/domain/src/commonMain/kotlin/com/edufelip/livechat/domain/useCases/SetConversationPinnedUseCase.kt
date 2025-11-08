package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IMessagesRepository

class SetConversationPinnedUseCase(
    private val repository: IMessagesRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long? = null,
    ) {
        repository.setConversationPinned(conversationId, pinned, pinnedAt)
    }
}
