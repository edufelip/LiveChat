package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.bridge.MessagesRemoteBridge
import com.edufelip.livechat.data.bridge.MessagesRemoteListener
import com.edufelip.livechat.data.bridge.TransportMessagePayload
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

private const val COMM_TAG = "COMMCHECK"

class FirebaseMessagesRemoteData(
    private val messagesBridge: MessagesRemoteBridge,
    private val storageBridge: MediaStorageBridge,
    private val config: FirebaseRestConfig,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IMessagesRemoteData {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): Flow<List<Message>> {
        if (!config.isConfigured) return flowOf(emptyList())
        val currentUserId = sessionProvider.currentUserId() ?: return flowOf(emptyList())

        return callbackFlow {
            val listener =
                object : MessagesRemoteListener {
                    override fun onMessages(messages: List<TransportMessagePayload>) {
                        trySend(messages)
                    }

                    override fun onError(message: String) {
                        log("observeConversation error: $message")
                        trySend(emptyList())
                    }
                }
            val token = messagesBridge.startListening(currentUserId, listener)
            awaitClose { messagesBridge.stopListening(token) }
        }.mapLatest { messages ->
            val mapped = mutableListOf<Message>()
            messages.forEach { payload ->
                if (!payload.isAddressedTo(currentUserId)) return@forEach
                if (conversationId.isNotBlank() && payload.senderId.orEmpty() != conversationId) return@forEach

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
                        put(META_RECEIVER_ID, payload.receiverId.orEmpty())
                        if (contentType.isMedia()) put(META_REMOTE_URL, remoteContent)
                        if (contentType.isMedia()) put(META_LOCAL_PATH, mediaPath)
                    }
                val message =
                    payload.toDomainMessage(
                        documentId = payload.id,
                        conversationId = payload.senderId.orEmpty(),
                        contentTypeOverride = contentType,
                        bodyOverride = mediaPath,
                        extraMetadata = extraMetadata,
                    )
                mapped += message
                deleteRemoteMessage(
                    recipientId = currentUserId,
                    documentId = payload.id,
                    payload = payload,
                    deleteMedia = contentType.isMedia(),
                )
            }
            mapped.sortBy { it.createdAt }
            sinceEpochMillis?.let { ts -> mapped.filter { it.createdAt > ts } } ?: mapped
        }.flowOn(dispatcher)
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
            val documentId = draft.localId.ifBlank { timestamp.toString() }
            val payload =
                draft.toTransportPayload(
                    senderId = senderId,
                    timestamp = timestamp,
                    content = mediaUpload?.downloadUrl ?: draft.body,
                    status = STATUS_PENDING,
                    documentId = documentId,
                )

            messagesBridge.sendMessage(
                recipientId = draft.conversationId,
                documentId = documentId,
                payload = payload,
            )

            val extraMetadata =
                buildMap<String, String> {
                    put(META_RECEIVER_ID, draft.conversationId)
                    mediaUpload?.let {
                        put(META_REMOTE_URL, it.downloadUrl)
                        put(META_LOCAL_PATH, draft.body)
                    }
                }

            Message(
                id = documentId,
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
        withContext(dispatcher) { messagesBridge.ensureConversation(conversationId) }
    }

    override suspend fun pullHistorical(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message> =
        withContext(dispatcher) {
            if (!config.isConfigured) return@withContext emptyList()

            val currentUserId = sessionProvider.currentUserId() ?: return@withContext emptyList()
            val mapped =
                runCatching { messagesBridge.fetchMessages(currentUserId) }
                    .onFailure { throwable ->
                        if (throwable.isPermissionDenied()) {
                            println("Firestore permission denied while pulling history: ${throwable.message}")
                        }
                    }.getOrDefault(emptyList())
                    .mapNotNull { payload ->
                        if (!payload.isAddressedTo(currentUserId)) return@mapNotNull null
                        if (conversationId.isNotBlank() && payload.senderId.orEmpty() != conversationId) return@mapNotNull null

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
                                put(META_RECEIVER_ID, payload.receiverId.orEmpty())
                                if (contentType.isMedia()) put(META_REMOTE_URL, remoteContent)
                                if (contentType.isMedia()) put(META_LOCAL_PATH, mediaPath)
                            }
                        val message =
                            payload.toDomainMessage(
                                documentId = payload.id,
                                conversationId = payload.senderId.orEmpty(),
                                contentTypeOverride = contentType,
                                bodyOverride = mediaPath,
                                extraMetadata = extraMetadata,
                            )
                        deleteRemoteMessage(
                            recipientId = currentUserId,
                            documentId = payload.id,
                            payload = payload,
                            deleteMedia = contentType.isMedia(),
                        )
                        message
                    }.sortedBy { it.createdAt }
            sinceEpochMillis?.let { ts -> mapped.filter { it.createdAt > ts } } ?: mapped
        }

    private fun TransportMessagePayload.isAddressedTo(userId: String): Boolean = receiverId?.isNotBlank() == true && receiverId == userId

    private fun TransportMessagePayload.timestampMillis(): Long? = createdAtMillis

    private fun TransportMessagePayload.toDomainMessage(
        documentId: String,
        conversationId: String = senderId.orEmpty(),
        contentTypeOverride: MessageContentType? = null,
        bodyOverride: String? = null,
        extraMetadata: Map<String, String> = emptyMap(),
    ): Message {
        val createdAt = timestampMillis() ?: currentEpochMillis()
        val contentType = contentTypeOverride ?: type.toMessageContentType()
        val status = status.toMessageStatus()
        val resolvedConversationId = conversationId.takeIf { it.isNotBlank() } ?: senderId.orEmpty()
        return Message(
            id = documentId,
            conversationId = resolvedConversationId,
            senderId = senderId.orEmpty(),
            body = bodyOverride ?: content.orEmpty(),
            createdAt = createdAt,
            status = status,
            contentType = contentType,
            metadata =
                buildMap {
                    put(META_RECEIVER_ID, receiverId.orEmpty())
                    putAll(extraMetadata)
                },
        )
    }

    private fun MessageDraft.toTransportPayload(
        senderId: String,
        timestamp: Long,
        content: String,
        status: String,
        documentId: String,
    ): TransportMessagePayload {
        val messageType = contentType.toTransportType()
        return TransportMessagePayload(
            id = documentId,
            senderId = senderId,
            receiverId = conversationId,
            createdAtMillis = timestamp,
            type = messageType,
            content = content,
            status = status,
        )
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

    private fun MessageContentType.isMedia(): Boolean = this == MessageContentType.Image || this == MessageContentType.Audio

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
        val objectPath = "messages/${draft.conversationId}/$senderId/$timestamp.$extension"
        val downloadUrl = storageBridge.uploadBytes(objectPath, bytes)
        return MediaUpload(downloadUrl = downloadUrl, objectPath = objectPath)
    }

    private suspend fun downloadMediaToLocal(
        remoteUrl: String,
        contentType: MessageContentType,
    ): String {
        val bytes = storageBridge.downloadBytes(remoteUrl, MAX_DOWNLOAD_BYTES)
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
        payload: TransportMessagePayload,
        deleteMedia: Boolean,
    ) {
        runCatching<Unit> { messagesBridge.deleteMessage(recipientId, documentId) }
        if (deleteMedia && payload.isMediaMessage()) {
            payload.content?.let { url ->
                runCatching<Unit> { storageBridge.deleteRemote(url) }
            }
        } else {
        }
    }

    private fun TransportMessagePayload.isMediaMessage(): Boolean {
        val normalized = type?.lowercase() ?: return false
        return normalized == "image" || normalized == "audio"
    }

    private data class MediaUpload(
        val downloadUrl: String,
        val objectPath: String,
    )

    private fun Throwable.isPermissionDenied(): Boolean = (this.message ?: "").contains("PERMISSION_DENIED", ignoreCase = true)

    private fun log(message: String) {
        println("$COMM_TAG: $message")
    }

    private companion object {
        const val STATUS_PENDING = "pending"
        const val META_REMOTE_URL = "remoteUrl"
        const val META_LOCAL_PATH = "localPath"
        const val META_RECEIVER_ID = "receiverId"
        const val MAX_DOWNLOAD_BYTES = 20L * 1024L * 1024L
    }
}
