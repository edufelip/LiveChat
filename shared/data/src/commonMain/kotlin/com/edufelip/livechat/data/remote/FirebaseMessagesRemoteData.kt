package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.domain.models.AttachmentRef
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.CipherInfo
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.toMilliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.random.Random

class FirebaseMessagesRemoteData(
    private val firestore: FirebaseFirestore,
    private val config: FirebaseRestConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : IMessagesRemoteData {
    override fun observeConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): Flow<List<Message>> {
        if (!config.isConfigured) return flowOf(emptyList())

        return queryForConversation(conversationId)
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toMessageOrNull() }
                    .sortedBy { it.createdAt }
            }
            .map { messages ->
                sinceEpochMillis?.let { ts -> messages.filter { it.createdAt > ts } } ?: messages
            }
            .flowOn(dispatcher)
    }

    override suspend fun sendMessage(draft: MessageDraft): Message =
        withContext(dispatcher) {
            require(config.isConfigured) { "Firebase projectId is missing – cannot send message" }

            val documentId = draft.resolveDocumentId()
            ensureConversation(draft.conversationId, draft.senderId, userPhone = null, peer = null)
            val document = messagesCollection(draft.conversationId).document(documentId)
            val payload = draft.toFirestorePayload()
            document.set(payload, merge = true)

            document.get().toMessageOrNull() ?: draft.toFallbackMessage(documentId)
        }

    override suspend fun ensureConversation(
        conversationId: String,
        userId: String,
        userPhone: String?,
        peer: ConversationPeer?,
    ) {
        withContext(dispatcher) {
            ensureConversationDocument(conversationId, userId, userPhone, peer)
        }
    }

    override suspend fun pullHistorical(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message> =
        withContext(dispatcher) {
            if (!config.isConfigured) return@withContext emptyList()

            val documents = queryForConversation(conversationId).get().documents
            val mapped =
                documents
                    .mapNotNull { it.toMessageOrNull() }
                    .sortedBy { it.createdAt }
            sinceEpochMillis?.let { ts -> mapped.filter { it.createdAt > ts } } ?: mapped
        }

    private fun queryForConversation(conversationId: String): Query {
        return messagesCollection(conversationId)
            .where { FIELD_CONVERSATION_ID equalTo conversationId }
            .orderBy(FIELD_CREATED_AT)
    }

    private fun messagesCollection(conversationId: String): CollectionReference =
        firestore
            .collection(config.conversationsCollection)
            .document(conversationId)
            .collection(config.messagesCollection)

    private suspend fun ensureConversationDocument(
        conversationId: String,
        userId: String,
        userPhone: String?,
        peer: ConversationPeer?,
    ) {
        val conversationRef = firestore.collection(config.conversationsCollection).document(conversationId)
        val snapshot = runCatching { conversationRef.get() }.getOrNull()
        val existing = runCatching { snapshot?.data(ConversationDoc.serializer()) }.getOrNull() ?: ConversationDoc()
        val participantUids = (existing.participant_uids ?: emptyList()).toMutableSet()
        participantUids += userId
        peer?.firebaseUid?.takeIf { it.isNotBlank() }?.let { participantUids += it }

        val participantPhones = (existing.participant_phones ?: emptyList()).toMutableSet()
        userPhone.asNormalizedPhone()?.let { participantPhones += it }
        peer?.phoneNumber.asNormalizedPhone()?.let { participantPhones += it }

        val updatedRoles = (existing.roles ?: emptyMap()).toMutableMap()
        updatedRoles[userId] = OWNER_ROLE
        peer?.firebaseUid?.takeIf { it.isNotBlank() }?.let { uid ->
            updatedRoles.putIfAbsent(uid, MEMBER_ROLE)
        }

        val payload = mutableMapOf<String, Any?>(
            FIELD_CONVERSATION_ID to conversationId,
            FIELD_PARTICIPANT_UIDS to participantUids.toList(),
        )
        if (participantPhones.isNotEmpty()) {
            payload[FIELD_PARTICIPANT_PHONES] = participantPhones.toList()
        }
        if (updatedRoles.isNotEmpty()) {
            payload[FIELD_ROLES] = updatedRoles
        }
        if (snapshot?.exists != true) {
            payload[FIELD_CREATED_AT] = FieldValue.serverTimestamp
            payload[FIELD_CREATED_BY] = userId
        }
        conversationRef.set(payload, merge = true)

        ensureParticipantDocument(
            conversationRef = conversationRef,
            userId = userId,
            role = OWNER_ROLE,
            normalizedPhone = userPhone.asNormalizedPhone(),
        )

        peer?.firebaseUid?.takeIf { it.isNotBlank() }?.let { peerUid ->
            ensureParticipantDocument(
                conversationRef = conversationRef,
                userId = peerUid,
                role = MEMBER_ROLE,
                normalizedPhone = peer.phoneNumber.asNormalizedPhone(),
            )
        }
    }

    private suspend fun ensureParticipantDocument(
        conversationRef: DocumentReference,
        userId: String,
        role: String,
        normalizedPhone: String?,
    ) {
        val participantRef = conversationRef.collection(PARTICIPANTS_COLLECTION).document(userId)
        val snapshot = runCatching { participantRef.get() }.getOrNull()
        if (snapshot?.exists == true) return
        val payload = mutableMapOf<String, Any?>(
            FIELD_USER_ID to userId,
            FIELD_ROLE to role,
            FIELD_JOINED_AT to FieldValue.serverTimestamp,
        )
        normalizedPhone?.let { payload[FIELD_PHONE_NUMBER] = it }
        participantRef.set(payload, merge = true)
    }

    private fun String?.asNormalizedPhone(): String? =
        this?.let(::normalizePhoneNumber)?.takeIf { it.isNotBlank() }

    @Serializable
    private data class ConversationDoc(
        val conversation_id: String? = null,
        val participant_uids: List<String>? = null,
        val participant_phones: List<String>? = null,
        val roles: Map<String, String>? = null,
        val created_by: String? = null,
    )

    private fun MessageDraft.resolveDocumentId(): String =
        localId.takeIf { it.isNotBlank() } ?: randomDocumentId()

    private fun MessageDraft.toFirestorePayload(): Map<String, Any?> =
        buildMap {
            put(FIELD_CONVERSATION_ID, conversationId)
            put(FIELD_SENDER_ID, senderId)
            put(FIELD_BODY, body)
            if (ciphertext != null) put(FIELD_CIPHERTEXT, ciphertext)
            put(FIELD_CREATED_AT, createdAt.takeIf { it != 0L } ?: FieldValue.serverTimestamp)
            put(FIELD_STATUS, MessageStatus.SENT.name)
            put(FIELD_LOCAL_TEMP_ID, localId)
            put(FIELD_CONTENT_TYPE, contentType.name)
            if (replyToMessageId != null) put(FIELD_REPLY_TO_MESSAGE_ID, replyToMessageId)
            if (threadRootId != null) put(FIELD_THREAD_ROOT_ID, threadRootId)
            if (attachments.isNotEmpty()) put(FIELD_ATTACHMENTS, encodeAttachments(attachments))
            if (metadata.isNotEmpty()) put(FIELD_METADATA, encodeMetadata(metadata))
            put(FIELD_SERVER_ACK_AT, FieldValue.serverTimestamp)
        }

    private fun MessageDraft.toFallbackMessage(serverId: String): Message =
        Message(
            id = serverId,
            conversationId = conversationId,
            senderId = senderId,
            body = body,
            createdAt = createdAt,
            status = MessageStatus.SENT,
            localTempId = localId,
            contentType = contentType,
            ciphertext = ciphertext,
            attachments = attachments,
            replyToMessageId = replyToMessageId,
            threadRootId = threadRootId,
            metadata = metadata,
        )

    private fun DocumentSnapshot.toMessageOrNull(): Message? {
        val doc = runCatching { data(MessageDoc.serializer()) }.getOrNull() ?: return null
        val conversationId = doc.conversationId().orEmpty()
        if (conversationId.isBlank()) return null

        val status =
            doc.status?.let { runCatching { MessageStatus.valueOf(it) }.getOrDefault(MessageStatus.SENT) }
                ?: MessageStatus.SENT
        val contentType =
            doc.contentType()?.let { runCatching { MessageContentType.valueOf(it) }.getOrDefault(MessageContentType.Text) }
                ?: MessageContentType.Text

        return Message(
            id = id.ifBlank { doc.localTempId().orEmpty() },
            conversationId = conversationId,
            senderId = doc.senderId().orEmpty(),
            body = doc.body.orEmpty(),
            createdAt = doc.createdAtMillis() ?: 0L,
            status = status,
            localTempId = doc.localTempId(),
            messageSeq = doc.messageSeq(),
            serverAckAt = doc.serverAckAtMillis(),
            contentType = contentType,
            ciphertext = doc.ciphertext,
            attachments = decodeAttachments(doc.attachments),
            replyToMessageId = doc.replyToMessageId(),
            threadRootId = doc.threadRootId(),
            editedAt = doc.editedAtMillis(),
            deletedForAllAt = doc.deletedForAllAtMillis(),
            metadata = decodeMetadata(doc.metadata),
        )
    }

    private fun encodeAttachments(attachments: List<AttachmentRef>): String =
        json.encodeToString(attachmentsSerializer, attachments.map { it.toPayload() })

    private fun decodeAttachments(raw: String?): List<AttachmentRef> =
        raw?.takeIf { it.isNotBlank() }
            ?.let { runCatching { json.decodeFromString(attachmentsSerializer, it) }.getOrNull() }
            ?.map { it.toDomain() }
            ?: emptyList()

    private fun encodeMetadata(metadata: Map<String, String>): String =
        json.encodeToString(metadataSerializer, metadata)

    private fun decodeMetadata(raw: String?): Map<String, String> =
        raw?.takeIf { it.isNotBlank() }
            ?.let { runCatching { json.decodeFromString(metadataSerializer, it) }.getOrDefault(emptyMap()) }
            ?: emptyMap()

    @Serializable
    private data class MessageDoc(
        val conversation_id: String? = null,
        val sender_id: String? = null,
        val body: String? = null,
        val created_at: Timestamp? = null,
        val created_at_long: Long? = null,
        val status: String? = null,
        val local_temp_id: String? = null,
        val message_seq: Long? = null,
        val server_ack_at: Timestamp? = null,
        val server_ack_at_long: Long? = null,
        val content_type: String? = null,
        val ciphertext: String? = null,
        val attachments: String? = null,
        val reply_to_message_id: String? = null,
        val thread_root_id: String? = null,
        val edited_at: Timestamp? = null,
        val deleted_for_all_at: Timestamp? = null,
        val metadata: String? = null,
    ) {
        fun conversationId(): String? = conversation_id
        fun senderId(): String? = sender_id
        fun createdAtMillis(): Long? = created_at?.toMilliseconds()?.toLong() ?: created_at_long
        fun serverAckAtMillis(): Long? = server_ack_at?.toMilliseconds()?.toLong() ?: server_ack_at_long
        fun editedAtMillis(): Long? = edited_at?.toMilliseconds()?.toLong()
        fun deletedForAllAtMillis(): Long? = deleted_for_all_at?.toMilliseconds()?.toLong()
        fun contentType(): String? = content_type
        fun localTempId(): String? = local_temp_id
        fun messageSeq(): Long? = message_seq
        fun replyToMessageId(): String? = reply_to_message_id
        fun threadRootId(): String? = thread_root_id
    }

    private fun Timestamp?.asEpochMillis(): Long? = this?.let { (seconds * 1_000L) + (nanoseconds / 1_000_000L) }

    @kotlinx.serialization.Serializable
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

    @kotlinx.serialization.Serializable
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

    private companion object {
        const val FIELD_CONVERSATION_ID = "conversation_id"
        const val FIELD_SENDER_ID = "sender_id"
        const val FIELD_BODY = "body"
        const val FIELD_CREATED_AT = "created_at"
        const val FIELD_STATUS = "status"
        const val FIELD_LOCAL_TEMP_ID = "local_temp_id"
        const val FIELD_MESSAGE_SEQ = "message_seq"
        const val FIELD_SERVER_ACK_AT = "server_ack_at"
        const val FIELD_CONTENT_TYPE = "content_type"
        const val FIELD_CIPHERTEXT = "ciphertext"
        const val FIELD_ATTACHMENTS = "attachments"
        const val FIELD_REPLY_TO_MESSAGE_ID = "reply_to_message_id"
        const val FIELD_THREAD_ROOT_ID = "thread_root_id"
        const val FIELD_EDITED_AT = "edited_at"
        const val FIELD_DELETED_FOR_ALL_AT = "deleted_for_all_at"
        const val FIELD_METADATA = "metadata"
        const val FIELD_PARTICIPANT_UIDS = "participant_uids"
        const val FIELD_PARTICIPANT_PHONES = "participant_phones"
        const val FIELD_ROLES = "roles"
        const val FIELD_USER_ID = "user_id"
        const val FIELD_ROLE = "role"
        const val FIELD_JOINED_AT = "joined_at"
        const val FIELD_PHONE_NUMBER = "phone_number"
        const val FIELD_CREATED_BY = "created_by"

        val attachmentsSerializer = ListSerializer(AttachmentPayload.serializer())
        val metadataSerializer = MapSerializer(String.serializer(), String.serializer())

        const val PARTICIPANTS_COLLECTION = "participants"
        const val OWNER_ROLE = "owner"
        const val MEMBER_ROLE = "member"
    }
}
    private fun randomDocumentId(): String {
        val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(20) {
            repeat(20) {
                append(alphabet[Random.nextInt(alphabet.length)])
            }
        }
    }
