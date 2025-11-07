package com.project.livechat.domain.useCases

import com.project.livechat.domain.repositories.IConversationParticipantsRepository

class SetConversationMutedUseCase(
    private val repository: IConversationParticipantsRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        muteUntil: Long?,
    ) {
        repository.setMuteUntil(conversationId, muteUntil)
    }
}
