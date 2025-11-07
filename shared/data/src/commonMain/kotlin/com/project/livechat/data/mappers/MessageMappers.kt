package com.project.livechat.data.mappers

import com.project.livechat.domain.models.AttachmentRef
import com.project.livechat.domain.models.CipherInfo
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageContentType
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.shared.data.database.MessageEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun MessageEntity.toDomain(): Message =
    Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        body = body,
        createdAt = createdAt,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        localTempId = localTempId,
        messageSeq = messageSeq,
        serverAckAt = serverAckAt,
        contentType = runCatching { MessageContentType.valueOf(contentType ?: MessageContentType.Text.name) }
            .getOrDefault(MessageContentType.Text),
        ciphertext = ciphertext,
        attachments = attachments?.let { AttachmentAdapter.decode(it) } ?: emptyList(),
        replyToMessageId = replyToMessageId,
        threadRootId = threadRootId,
        editedAt = editedAt,
        deletedForAllAt = deletedForAllAt,
        metadata = metadata?.let { MetadataAdapter.decode(it) } ?: emptyMap(),
    )

fun Message.toEntity(): MessageEntity =
    MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        body = body,
        createdAt = createdAt,
        status = status.name,
        localTempId = localTempId,
        messageSeq = messageSeq,
        serverAckAt = serverAckAt,
        contentType = contentType.name,
        ciphertext = ciphertext,
        attachments = attachments.takeIf { it.isNotEmpty() }?.let { AttachmentAdapter.encode(it) },
        replyToMessageId = replyToMessageId,
        threadRootId = threadRootId,
        editedAt = editedAt,
        deletedForAllAt = deletedForAllAt,
        metadata = metadata.takeIf { it.isNotEmpty() }?.let { MetadataAdapter.encode(it) },
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

internal object MetadataAdapter {
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
