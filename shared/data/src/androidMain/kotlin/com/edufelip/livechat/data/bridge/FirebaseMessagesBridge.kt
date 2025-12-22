package com.edufelip.livechat.data.bridge

import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseMessagesBridge(
    private val firestore: FirebaseFirestore,
    private val config: FirebaseRestConfig,
) : MessagesRemoteBridge {
    private val listeners = mutableMapOf<String, ListenerRegistration>()

    override fun startListening(
        recipientId: String,
        listener: MessagesRemoteListener,
    ): String {
        val token = UUID.randomUUID().toString()
        val registration =
            inboundMessagesCollection(recipientId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        listener.onError(error.message ?: "Failed to listen for messages.")
                        return@addSnapshotListener
                    }
                    val payloads = snapshot?.documents?.mapNotNull { it.toPayloadOrNull() }.orEmpty()
                    listener.onMessages(payloads)
                }
        listeners[token] = registration
        return token
    }

    override fun stopListening(token: String) {
        listeners.remove(token)?.remove()
    }

    override suspend fun fetchMessages(recipientId: String): List<TransportMessagePayload> {
        val snapshot = inboundMessagesCollection(recipientId).get().await()
        return snapshot.documents.mapNotNull { it.toPayloadOrNull() }
    }

    override suspend fun sendMessage(
        recipientId: String,
        documentId: String,
        payload: TransportMessagePayload,
    ): String {
        val document = inboundMessagesCollection(recipientId).document(documentId)
        document.set(payload.toFirestorePayload()).await()
        return document.id
    }

    override suspend fun deleteMessage(
        recipientId: String,
        documentId: String,
    ) {
        inboundMessagesCollection(recipientId).document(documentId).delete().await()
    }

    override suspend fun ensureConversation(conversationId: String) {
        firestore.collection(config.conversationsCollection)
            .document(conversationId)
            .set(mapOf(FIELD_CREATED_AT to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }

    private fun inboundMessagesCollection(recipientId: String) =
        firestore.collection(config.conversationsCollection)
            .document(recipientId)
            .collection(config.messagesCollection)

    private fun DocumentSnapshot.toPayloadOrNull(): TransportMessagePayload? =
        runCatching {
            TransportMessagePayload(
                id = id,
                senderId = getString(FIELD_SENDER_ID),
                receiverId = getString(FIELD_RECEIVER_ID),
                createdAtMillis =
                    getTimestamp(FIELD_CREATED_AT)?.toDate()?.time
                        ?: getLong(FIELD_CREATED_AT_MS),
                payloadType = getString(FIELD_PAYLOAD_TYPE),
                type = getString(FIELD_TYPE),
                content = getString(FIELD_CONTENT),
                status = getString(FIELD_STATUS),
                actionType = getString(FIELD_ACTION_TYPE),
                actionMessageId = getString(FIELD_ACTION_MESSAGE_ID),
            )
        }.getOrNull()

    private fun TransportMessagePayload.toFirestorePayload(): Map<String, Any?> =
        buildMap {
            put(FIELD_SENDER_ID, senderId)
            put(FIELD_RECEIVER_ID, receiverId)
            put(FIELD_CREATED_AT, FieldValue.serverTimestamp())
            put(FIELD_CREATED_AT_MS, createdAtMillis)
            put(FIELD_PAYLOAD_TYPE, payloadType)
            put(FIELD_TYPE, type)
            put(FIELD_CONTENT, content)
            put(FIELD_STATUS, status)
            put(FIELD_ACTION_TYPE, actionType)
            put(FIELD_ACTION_MESSAGE_ID, actionMessageId)
        }

    private companion object {
        const val FIELD_SENDER_ID = "sender_id"
        const val FIELD_RECEIVER_ID = "receiver_id"
        const val FIELD_CREATED_AT = "created_at"
        const val FIELD_CREATED_AT_MS = "created_at_ms"
        const val FIELD_PAYLOAD_TYPE = "payload_type"
        const val FIELD_TYPE = "type"
        const val FIELD_CONTENT = "content"
        const val FIELD_STATUS = "status"
        const val FIELD_ACTION_TYPE = "action_type"
        const val FIELD_ACTION_MESSAGE_ID = "action_message_id"
    }
}
