package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.repositories.IMessagesRepository

class SendMessageUseCase(
    private val messagesRepository: IMessagesRepository
) {
    suspend operator fun invoke(draft: MessageDraft): Message {
        return messagesRepository.sendMessage(draft)
    }
}
