package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository

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
