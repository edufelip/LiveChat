package com.project.livechat.data.mappers

import com.project.livechat.domain.models.AttachmentRef
import com.project.livechat.domain.models.CipherInfo
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageContentType
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.database.Messages
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Messages.toDomain(): Message =
    Message(
        id = id,
        conversationId = conversation_id,
        senderId = sender_id,
        body = body,
        createdAt = created_at,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        localTempId = local_temp_id,
        messageSeq = message_seq,
        serverAckAt = server_ack_at,
        contentType = runCatching { MessageContentType.valueOf(content_type ?: MessageContentType.Text.name) }.getOrDefault(MessageContentType.Text),
        ciphertext = ciphertext,
        attachments = attachments?.let { AttachmentAdapter.decode(it) } ?: emptyList(),
        replyToMessageId = reply_to_message_id,
        threadRootId = thread_root_id,
        editedAt = edited_at,
        deletedForAllAt = deleted_for_all_at,
        metadata = metadata?.let { MetadataAdapter.decode(it) } ?: emptyMap(),
    )

fun Message.toInsertParams(): InsertMessageParams =
    InsertMessageParams(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        body = body,
        createdAt = createdAt,
        status = status,
        localTempId = localTempId,
        messageSeq = messageSeq,
        serverAckAt = serverAckAt,
        contentType = contentType,
        ciphertext = ciphertext,
        attachments = attachments,
        replyToMessageId = replyToMessageId,
        threadRootId = threadRootId,
        editedAt = editedAt,
        deletedForAllAt = deletedForAllAt,
        metadata = metadata,
    )

fun MessageDraft.toPendingMessage(status: MessageStatus = MessageStatus.SENDING): Message =
    Message(
        id = localId,
        conversationId = conversationId,
        senderId = senderId,
        body = body,
        createdAt = createdAt,
        status = status,
        localTempId = localId,
        contentType = contentType,
        ciphertext = ciphertext,
        attachments = attachments,
        replyToMessageId = replyToMessageId,
        threadRootId = threadRootId,
        metadata = metadata,
    )

data class InsertMessageParams(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val createdAt: Long,
    val status: MessageStatus,
    val localTempId: String?,
    val messageSeq: Long?,
    val serverAckAt: Long?,
    val contentType: MessageContentType,
    val ciphertext: String?,
    val attachments: List<AttachmentRef>,
    val replyToMessageId: String?,
    val threadRootId: String?,
    val editedAt: Long?,
    val deletedForAllAt: Long?,
    val metadata: Map<String, String>,
)

fun LiveChatDatabase.insertMessage(params: InsertMessageParams) {
    messagesQueries.insertMessage(
        id = params.id,
        conversation_id = params.conversationId,
        sender_id = params.senderId,
        body = params.body,
        created_at = params.createdAt,
        status = params.status.name,
        local_temp_id = params.localTempId,
        message_seq = params.messageSeq,
        server_ack_at = params.serverAckAt,
        content_type = params.contentType.name,
        ciphertext = params.ciphertext,
        attachments = params.attachments.takeIf { it.isNotEmpty() }?.let { AttachmentAdapter.encode(it) },
        reply_to_message_id = params.replyToMessageId,
        thread_root_id = params.threadRootId,
        edited_at = params.editedAt,
        deleted_for_all_at = params.deletedForAllAt,
        metadata = params.metadata.takeIf { it.isNotEmpty() }?.let { MetadataAdapter.encode(it) },
    )
}

fun LiveChatDatabase.insertMessages(messages: List<InsertMessageParams>) {
    messagesQueries.transaction {
        messages.forEach { insertMessage(it) }
    }
}

fun LiveChatDatabase.clearConversation(conversationId: String) {
    messagesQueries.clearConversationMessages(conversationId)
}

fun List<Message>.toInsertParams(): List<InsertMessageParams> = map { it.toInsertParams() }

private val messageJson = Json { ignoreUnknownKeys = true }
private val attachmentListSerializer = ListSerializer(AttachmentPayload.serializer())
private val metadataMapSerializer = MapSerializer(String.serializer(), String.serializer())

private object AttachmentAdapter {
    fun encode(attachments: List<AttachmentRef>): String =
        messageJson.encodeToString(attachmentListSerializer, attachments.map { it.toPayload() })

    fun decode(raw: String): List<AttachmentRef> =
        runCatching { messageJson.decodeFromString(attachmentListSerializer, raw) }
            .getOrElse { emptyList() }
            .map { it.toDomain() }
}

private object MetadataAdapter {
    fun encode(metadata: Map<String, String>): String =
        messageJson.encodeToString(metadataMapSerializer, metadata)

    fun decode(raw: String): Map<String, String> =
        runCatching { messageJson.decodeFromString(metadataMapSerializer, raw) }
            .getOrDefault(emptyMap())
}

@Serializable
private data class AttachmentPayload(
    val objectKey: String,
    val mimeType: String,
    val sizeBytes: Long,
    val thumbnailKey: String? = null,
    val cipherInfo: CipherInfoPayload? = null,
) {
    fun toDomain(): AttachmentRef =
        AttachmentRef(
            objectKey = objectKey,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            thumbnailKey = thumbnailKey,
            cipherInfo = cipherInfo?.toDomain(),
        )
}

@Serializable
private data class CipherInfoPayload(
    val algorithm: String,
    val keyId: String,
    val nonce: String,
    val associatedData: String? = null,
) {
    fun toDomain(): CipherInfo =
        CipherInfo(
            algorithm = algorithm,
            keyId = keyId,
            nonce = nonce,
            associatedData = associatedData,
        )
}

private fun AttachmentRef.toPayload(): AttachmentPayload =
    AttachmentPayload(
        objectKey = objectKey,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        thumbnailKey = thumbnailKey,
        cipherInfo = cipherInfo?.toPayload(),
    )

private fun CipherInfo.toPayload(): CipherInfoPayload =
    CipherInfoPayload(
        algorithm = algorithm,
        keyId = keyId,
        nonce = nonce,
        associatedData = associatedData,
    )
