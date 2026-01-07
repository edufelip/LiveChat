package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.data.models.InboxAction
import com.edufelip.livechat.data.models.InboxItem
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import kotlinx.coroutines.flow.Flow

interface IMessagesRemoteData {
    fun observeConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): Flow<List<InboxItem>>

    suspend fun sendMessage(draft: MessageDraft): Message

    suspend fun pullHistorical(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message>

    suspend fun downloadMediaToLocal(
        remoteUrl: String,
        contentType: MessageContentType,
    ): String

    suspend fun deleteMedia(remoteUrl: String)

    suspend fun sendAction(action: InboxAction)

    suspend fun ensureConversation(
        conversationId: String,
        userId: String,
        userPhone: String?,
        peer: ConversationPeer?,
    )

    suspend fun purgeInboxMessagesFromSender(senderId: String)
}
