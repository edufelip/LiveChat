package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.repositories.IMessagesRepository

class SendMessageUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(draft: MessageDraft): Message = messagesRepository.sendMessage(draft)
}
