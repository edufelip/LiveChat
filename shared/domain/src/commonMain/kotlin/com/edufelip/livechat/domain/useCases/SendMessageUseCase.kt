package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.repositories.IMessagesRepository

class SendMessageUseCase(
    private val messagesRepository: IMessagesRepository,
) {
    private val logTag = "MSG_ATTRIBUTION"

    suspend operator fun invoke(draft: MessageDraft): Message {
        println(
            "$logTag: [SEND] Sending message - conversationId=${draft.conversationId}, " +
                "senderId=${draft.senderId}, body=${draft.body.take(20)}",
        )
        val sentMessage = messagesRepository.sendMessage(draft)
        println(
            "$logTag: [SEND] Message sent - id=${sentMessage.id}, " +
                "senderId=${sentMessage.senderId}, conversationId=${sentMessage.conversationId}",
        )
        return sentMessage
    }
}
