package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.utils.currentEpochMillis
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.toMilliseconds
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

private const val COMM_TAG = "COMMCHECK"

class FirebaseMessagesRemoteData(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val config: FirebaseRestConfig,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IMessagesRemoteData {
    override fun observeConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): Flow<List<Message>> {
        if (!config.isConfigured) return flowOf(emptyList())
        val currentUserId = sessionProvider.currentUserId() ?: return flowOf(emptyList())

        return inboundMessagesCollection(currentUserId)
            .snapshots
            .mapLatest { snapshot ->
                val mapped = mutableListOf<Message>()
                snapshot.documents.forEach { document ->
                    val payload = document.toTransportMessageOrNull() ?: return@forEach
                    if (!payload.isAddressedTo(currentUserId)) return@forEach
                    if (conversationId.isNotBlank() && payload.sender_id.orEmpty() != conversationId) return@forEach

                    val contentType = payload.type.toMessageContentType()
                    val remoteContent = payload.content.orEmpty()
                    val mediaPath =
                        if (contentType.isMedia()) {
                            downloadMediaToLocal(remoteContent, contentType)
                        } else {
                            remoteContent
                        }
                    val extraMetadata =
                        buildMap {
                            put(META_RECEIVER_ID, payload.receiver_id.orEmpty())
                            if (contentType.isMedia()) put(META_REMOTE_URL, remoteContent)
                            if (contentType.isMedia()) put(META_LOCAL_PATH, mediaPath)
                        }
                    val message =
                        payload.toDomainMessage(
                            documentId = document.id,
                            conversationId = payload.sender_id.orEmpty(),
                            contentTypeOverride = contentType,
                            bodyOverride = mediaPath,
                            extraMetadata = extraMetadata,
                        )
                    mapped += message
                    deleteRemoteMessage(
                        recipientId = currentUserId,
                        documentId = document.id,
                        payload = payload,
                        deleteMedia = contentType.isMedia(),
                    )
                }
                mapped.sortBy { it.createdAt }
                sinceEpochMillis?.let { ts -> mapped.filter { it.createdAt > ts } } ?: mapped
            }
            .flowOn(dispatcher)
    }

    override suspend fun sendMessage(draft: MessageDraft): Message =
        withContext(dispatcher) {
            require(config.isConfigured) { "Firebase projectId is missing – cannot send message" }
            require(draft.conversationId.isNotBlank()) { "Recipient conversationId is required to send messages" }

            val senderId =
                draft.senderId.takeIf { it.isNotBlank() }
                    ?: sessionProvider.currentUserId()
                        ?: error("Cannot send message without a senderId")
            val timestamp = draft.createdAt.takeIf { it != 0L } ?: currentEpochMillis()
            val mediaUpload = uploadMediaIfNeeded(draft, senderId, timestamp)
            val payload =
                draft.toTransportPayload(
                    senderId = senderId,
                    timestamp = timestamp,
                    content = mediaUpload?.downloadUrl ?: draft.body,
                    status = STATUS_PENDING,
                )

            val outboundCollection = outboundMessagesCollection(draft.conversationId)
            val document = outboundCollection.document(draft.localId.ifBlank { timestamp.toString() })
            document.set(payload, merge = false)

            val extraMetadata =
                buildMap<String, String> {
                    put(META_RECEIVER_ID, draft.conversationId)
                    mediaUpload?.let {
                        put(META_REMOTE_URL, it.downloadUrl)
                        put(META_LOCAL_PATH, draft.body)
                    }
                }

            Message(
                id = document.id,
                conversationId = draft.conversationId,
                senderId = senderId,
                body = draft.body,
                createdAt = timestamp,
                status = MessageStatus.DELIVERED,
                contentType = draft.contentType,
                metadata = extraMetadata,
            )
        }

    override suspend fun ensureConversation(
        conversationId: String,
        userId: String,
        userPhone: String?,
        peer: ConversationPeer?,
    ) {
        if (!config.isConfigured) return
        withContext(dispatcher) {
            firestore.collection(config.conversationsCollection)
                .document(conversationId)
                .set(mapOf(FIELD_CREATED_AT to FieldValue.serverTimestamp), merge = true)
        }
    }

    override suspend fun pullHistorical(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message> =
        withContext(dispatcher) {
            if (!config.isConfigured) return@withContext emptyList()

            val currentUserId = sessionProvider.currentUserId() ?: return@withContext emptyList()
            val documents =
                runCatching { inboundMessagesCollection(currentUserId).get().documents }
                    .onFailure { throwable ->
                        if (throwable.isPermissionDenied()) {
                            println("Firestore permission denied while pulling history: ${throwable.message}")
                        }
                    }.getOrNull()
                    ?: return@withContext emptyList()
            val mapped =
                documents.mapNotNull { document ->
                    val payload = document.toTransportMessageOrNull() ?: return@mapNotNull null
                    if (!payload.isAddressedTo(currentUserId)) return@mapNotNull null
                    if (conversationId.isNotBlank() && payload.sender_id.orEmpty() != conversationId) return@mapNotNull null

                    val contentType = payload.type.toMessageContentType()
                    val remoteContent = payload.content.orEmpty()
                    val mediaPath =
                        if (contentType.isMedia()) {
                            downloadMediaToLocal(remoteContent, contentType)
                        } else {
                            remoteContent
                        }
                    val extraMetadata =
                        buildMap {
                            put(META_RECEIVER_ID, payload.receiver_id.orEmpty())
                            if (contentType.isMedia()) put(META_REMOTE_URL, remoteContent)
                            if (contentType.isMedia()) put(META_LOCAL_PATH, mediaPath)
                        }
                    val message =
                        payload.toDomainMessage(
                            documentId = document.id,
                            conversationId = payload.sender_id.orEmpty(),
                            contentTypeOverride = contentType,
                            bodyOverride = mediaPath,
                            extraMetadata = extraMetadata,
                        )
                    deleteRemoteMessage(
                        recipientId = currentUserId,
                        documentId = document.id,
                        payload = payload,
                        deleteMedia = contentType.isMedia(),
                    )
                    message
                }
                    .sortedBy { it.createdAt }
            sinceEpochMillis?.let { ts -> mapped.filter { it.createdAt > ts } } ?: mapped
        }

    private fun inboundMessagesCollection(recipientId: String) =
        firestore.collection(config.conversationsCollection)
            .document(recipientId)
            .collection(config.messagesCollection)

    private fun outboundMessagesCollection(recipientId: String) =
        firestore.collection(config.conversationsCollection)
            .document(recipientId)
            .collection(config.messagesCollection)

    private fun DocumentSnapshot.toTransportMessageOrNull(): TransportMessageDoc? =
        runCatching<TransportMessageDoc> {
            data(
                strategy = TransportMessageDoc.serializer(),
                serverTimestampBehavior = ServerTimestampBehavior.NONE,
            )
        }.getOrNull()

    private fun TransportMessageDoc.isAddressedTo(userId: String): Boolean =
        receiver_id?.isNotBlank() == true && receiver_id == userId

    private fun TransportMessageDoc.timestampMillis(): Long? =
        created_at?.toMilliseconds()?.toLong() ?: created_at_ms

    private fun TransportMessageDoc.toDomainMessage(
        documentId: String,
        conversationId: String = sender_id.orEmpty(),
        contentTypeOverride: MessageContentType? = null,
        bodyOverride: String? = null,
        extraMetadata: Map<String, String> = emptyMap(),
    ): Message {
        val createdAt = timestampMillis() ?: currentEpochMillis()
        val contentType = contentTypeOverride ?: type.toMessageContentType()
        val status = status.toMessageStatus()
        val resolvedConversationId = conversationId.takeIf { it.isNotBlank() } ?: sender_id.orEmpty()
        return Message(
            id = documentId,
            conversationId = resolvedConversationId,
            senderId = sender_id.orEmpty(),
            body = bodyOverride ?: content.orEmpty(),
            createdAt = createdAt,
            status = status,
            contentType = contentType,
            metadata =
                buildMap {
                    put(META_RECEIVER_ID, receiver_id.orEmpty())
                    putAll(extraMetadata)
                },
        )
    }

    private fun MessageDraft.toTransportPayload(
        senderId: String,
        timestamp: Long,
        content: String,
        status: String,
    ): Map<String, Any?> {
        val messageType = contentType.toTransportType()
        return buildMap {
            put(FIELD_SENDER_ID, senderId)
            put(FIELD_RECEIVER_ID, conversationId)
            put(FIELD_CREATED_AT, FieldValue.serverTimestamp)
            put(FIELD_CREATED_AT_MS, timestamp)
            put(FIELD_TYPE, messageType)
            put(FIELD_CONTENT, content)
            put(FIELD_STATUS, status)
        }
    }

    private fun String?.toMessageContentType(): MessageContentType =
        when (this?.lowercase()) {
            "image" -> MessageContentType.Image
            "audio" -> MessageContentType.Audio
            else -> MessageContentType.Text
        }

    private fun MessageContentType.toTransportType(): String =
        when (this) {
            MessageContentType.Image -> "image"
            MessageContentType.Audio -> "audio"
            else -> "text"
        }

    private fun String?.toMessageStatus(): MessageStatus =
        when (this?.lowercase()) {
            "pending" -> MessageStatus.SENDING
            "delivered" -> MessageStatus.DELIVERED
            else -> MessageStatus.SENT
        }

    private fun MessageContentType.isMedia(): Boolean =
        this == MessageContentType.Image || this == MessageContentType.Audio

    private suspend fun uploadMediaIfNeeded(
        draft: MessageDraft,
        senderId: String,
        timestamp: Long,
    ): MediaUpload? {
        if (!draft.contentType.isMedia()) return null
        val localPath = draft.body
        val bytes = MediaFileStore.readBytes(localPath) ?: error("Missing media at $localPath")
        val extension =
            when (draft.contentType) {
                MessageContentType.Image -> "jpg"
                MessageContentType.Audio -> "m4a"
                else -> "bin"
            }
        val objectPath = "messages/${draft.conversationId}/${senderId}/${timestamp}.$extension"
        val downloadUrl = storage.uploadBytes(objectPath, bytes)
        return MediaUpload(downloadUrl = downloadUrl, objectPath = objectPath)
    }

    private suspend fun downloadMediaToLocal(
        remoteUrl: String,
        contentType: MessageContentType,
    ): String {
        val bytes = storage.downloadBytes(remoteUrl, MAX_DOWNLOAD_BYTES)
        val extension =
            when (contentType) {
                MessageContentType.Image -> "jpg"
                MessageContentType.Audio -> "m4a"
                else -> "bin"
            }
        return MediaFileStore.saveBytes(prefix = "msg_", extension = extension, data = bytes)
    }

    private suspend fun deleteRemoteMessage(
        recipientId: String,
        documentId: String,
        payload: TransportMessageDoc,
        deleteMedia: Boolean,
    ) {
        runCatching<Unit> { inboundMessagesCollection(recipientId).document(documentId).delete() }
        if (deleteMedia && payload.isMediaMessage()) {
            payload.content?.let { url ->
                runCatching<Unit> { storage.deleteRemote(url) }
            }
        } else {
        }
    }

    private fun TransportMessageDoc.isMediaMessage(): Boolean {
        val normalized = type?.lowercase() ?: return false
        return normalized == "image" || normalized == "audio"
    }

    private data class MediaUpload(
        val downloadUrl: String,
        val objectPath: String,
    )

    private fun Throwable.isPermissionDenied(): Boolean =
        (this.message ?: "").contains("PERMISSION_DENIED", ignoreCase = true)

    private fun log(message: String) {
        println("$COMM_TAG: $message")
    }

    private companion object {
        const val FIELD_SENDER_ID = "sender_id"
        const val FIELD_RECEIVER_ID = "receiver_id"
        const val FIELD_CREATED_AT = "created_at"
        const val FIELD_CREATED_AT_MS = "created_at_ms"
        const val FIELD_TYPE = "type"
        const val FIELD_CONTENT = "content"
        const val FIELD_STATUS = "status"
        const val STATUS_PENDING = "pending"
        const val STATUS_DELIVERED = "delivered"
        const val META_REMOTE_URL = "remoteUrl"
        const val META_LOCAL_PATH = "localPath"
        const val META_RECEIVER_ID = "receiverId"
        const val MAX_DOWNLOAD_BYTES = 20L * 1024L * 1024L
    }
}

@Serializable
private data class TransportMessageDoc(
    val sender_id: String? = null,
    val receiver_id: String? = null,
    val created_at: Timestamp? = null,
    val created_at_ms: Long? = null,
    val type: String? = null,
    val content: String? = null,
    val status: String? = null,
)
