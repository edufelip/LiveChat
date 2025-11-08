package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository

class SetConversationArchivedUseCase(
    private val repository: IConversationParticipantsRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        archived: Boolean,
    ) {
        repository.setArchived(conversationId, archived)
    }
}
