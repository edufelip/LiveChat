package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Participant
import com.project.livechat.domain.repositories.IConversationParticipantsRepository
import kotlinx.coroutines.flow.Flow

class ObserveParticipantUseCase(
    private val repository: IConversationParticipantsRepository,
) {
    operator fun invoke(conversationId: String): Flow<Participant?> =
        repository.observeParticipant(conversationId)
}
