package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationSummariesUseCase(
    private val repository: IMessagesRepository,
) {
    operator fun invoke(): Flow<List<ConversationSummary>> = repository.observeConversationSummaries()
}
