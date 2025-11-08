package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import kotlinx.coroutines.flow.Flow

class ObserveParticipantUseCase(
    private val repository: IConversationParticipantsRepository,
) {
    operator fun invoke(conversationId: String): Flow<Participant?> =
        repository.observeParticipant(conversationId)
}
