package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationSummariesUseCase(
    private val repository: IMessagesRepository
) {
    operator fun invoke(): Flow<List<ConversationSummary>> = repository.observeConversationSummaries()
}
