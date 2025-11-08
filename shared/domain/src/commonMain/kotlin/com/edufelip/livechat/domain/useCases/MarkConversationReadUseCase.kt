package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IMessagesRepository

class MarkConversationReadUseCase(
    private val repository: IMessagesRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long? = null,
    ) {
        repository.markConversationAsRead(conversationId, lastReadAt, lastReadSeq)
    }
}
