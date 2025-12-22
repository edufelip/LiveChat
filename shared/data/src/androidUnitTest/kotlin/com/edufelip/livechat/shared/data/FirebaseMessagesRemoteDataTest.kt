package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.bridge.MessagesRemoteBridge
import com.edufelip.livechat.data.bridge.MessagesRemoteListener
import com.edufelip.livechat.data.bridge.TransportMessagePayload
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseMessagesRemoteDataTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun sendImageMessageUploadsMediaAndSendsDownloadUrl() =
        runTest(dispatcher) {
            val bridge = RecordingMessagesBridge()
            val storage = RecordingStorageBridge(downloadUrl = "https://example.com/file.jpg")
            val session = FakeSessionProvider(userId = "user-a")
            val remote =
                FirebaseMessagesRemoteData(
                    messagesBridge = bridge,
                    storageBridge = storage,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    sessionProvider = session,
                    dispatcher = dispatcher,
                )

            val temp = File.createTempFile("img", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val draft =
                MessageDraft(
                    conversationId = "user-b",
                    senderId = "",
                    body = temp.absolutePath,
                    localId = "local-1",
                    createdAt = 1234L,
                    contentType = MessageContentType.Image,
                )

            val sent = remote.sendMessage(draft)

            val payload = bridge.lastPayload
            assertNotNull(payload)
            assertEquals("user-b", bridge.lastRecipient)
            assertEquals("message", payload.payloadType)
            assertEquals("image", payload.type)
            assertEquals("https://example.com/file.jpg", payload.content)
            assertEquals("sent", payload.status)
            assertTrue(storage.uploadedPaths.single().contains("/user-a/1234"))
            assertEquals("local-1", sent.id)
        }

    @Test
    fun sendAudioMessageUploadsMediaAndSendsDownloadUrl() =
        runTest(dispatcher) {
            val bridge = RecordingMessagesBridge()
            val storage = RecordingStorageBridge(downloadUrl = "https://example.com/file.m4a")
            val session = FakeSessionProvider(userId = "user-a")
            val remote =
                FirebaseMessagesRemoteData(
                    messagesBridge = bridge,
                    storageBridge = storage,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    sessionProvider = session,
                    dispatcher = dispatcher,
                )

            val temp = File.createTempFile("audio", ".m4a").apply { writeBytes(byteArrayOf(4, 5, 6)) }
            val draft =
                MessageDraft(
                    conversationId = "user-b",
                    senderId = "",
                    body = temp.absolutePath,
                    localId = "local-2",
                    createdAt = 2345L,
                    contentType = MessageContentType.Audio,
                )

            remote.sendMessage(draft)

            val payload = bridge.lastPayload
            assertNotNull(payload)
            assertEquals("audio", payload.type)
            assertEquals("https://example.com/file.m4a", payload.content)
            assertTrue(storage.uploadedPaths.single().contains("/user-a/2345"))
        }

    @Test
    fun observeConversationIgnoresMalformedActionAndDeletesIt() =
        runTest(dispatcher) {
            val bridge = EmittingMessagesBridge()
            val storage = RecordingStorageBridge(downloadUrl = "")
            val session = FakeSessionProvider(userId = "user-a")
            val remote =
                FirebaseMessagesRemoteData(
                    messagesBridge = bridge,
                    storageBridge = storage,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    sessionProvider = session,
                    dispatcher = dispatcher,
                )

            var items: List<com.edufelip.livechat.data.models.InboxItem> = emptyList()
            val job =
                launch {
                    remote.observeConversation("user-b", null).take(1).collect { items = it }
                }
            advanceUntilIdle()
            bridge.emit(
                TransportMessagePayload(
                    id = "action-1",
                    senderId = "user-b",
                    receiverId = "user-a",
                    createdAtMillis = 10L,
                    payloadType = "action",
                    actionType = "delivered",
                    actionMessageId = null,
                ),
            )
            advanceUntilIdle()
            job.cancel()
            assertTrue(items.isEmpty())
            assertEquals(listOf("action-1"), bridge.deletedIds)
        }

    @Test
    fun observeConversationEmitsMessageEvenWhenDeleteFails() =
        runTest(dispatcher) {
            val bridge = EmittingMessagesBridge().apply { failDeletes = true }
            val storage = RecordingStorageBridge(downloadUrl = "")
            val session = FakeSessionProvider(userId = "user-a")
            val remote =
                FirebaseMessagesRemoteData(
                    messagesBridge = bridge,
                    storageBridge = storage,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    sessionProvider = session,
                    dispatcher = dispatcher,
                )

            var items: List<com.edufelip.livechat.data.models.InboxItem> = emptyList()
            val job =
                launch {
                    remote.observeConversation("user-b", null).take(1).collect { items = it }
                }
            advanceUntilIdle()
            bridge.emit(
                TransportMessagePayload(
                    id = "msg-1",
                    senderId = "user-b",
                    receiverId = "user-a",
                    createdAtMillis = 100L,
                    payloadType = "message",
                    type = "text",
                    content = "hello",
                    status = "sent",
                ),
            )
            advanceUntilIdle()
            job.cancel()
            assertEquals(1, items.size)
            assertTrue(bridge.deletedIds.contains("msg-1"))
        }

    @Test
    fun sendMessageFailsWhenMediaUploadFails() =
        runTest(dispatcher) {
            val bridge = RecordingMessagesBridge()
            val storage = FailingStorageBridge()
            val session = FakeSessionProvider(userId = "user-a")
            val remote =
                FirebaseMessagesRemoteData(
                    messagesBridge = bridge,
                    storageBridge = storage,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    sessionProvider = session,
                    dispatcher = dispatcher,
                )

            val temp = File.createTempFile("img", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val draft =
                MessageDraft(
                    conversationId = "user-b",
                    senderId = "",
                    body = temp.absolutePath,
                    localId = "local-3",
                    createdAt = 3456L,
                    contentType = MessageContentType.Image,
                )

            val result = runCatching { remote.sendMessage(draft) }
            assertTrue(result.isFailure)
            assertTrue(storage.uploaded)
        }

    private class RecordingMessagesBridge : MessagesRemoteBridge {
        var lastRecipient: String? = null
        var lastPayload: TransportMessagePayload? = null

        override fun startListening(
            recipientId: String,
            listener: MessagesRemoteListener,
        ): String = "token"

        override fun stopListening(token: String) {
        }

        override suspend fun fetchMessages(recipientId: String): List<TransportMessagePayload> = emptyList()

        override suspend fun sendMessage(
            recipientId: String,
            documentId: String,
            payload: TransportMessagePayload,
        ): String {
            lastRecipient = recipientId
            lastPayload = payload
            return documentId
        }

        override suspend fun deleteMessage(
            recipientId: String,
            documentId: String,
        ) {
        }

        override suspend fun ensureConversation(conversationId: String) {
        }
    }

    private class EmittingMessagesBridge : MessagesRemoteBridge {
        private var listener: MessagesRemoteListener? = null
        val deletedIds = mutableListOf<String>()
        var failDeletes: Boolean = false

        override fun startListening(
            recipientId: String,
            listener: MessagesRemoteListener,
        ): String {
            this.listener = listener
            return "token"
        }

        override fun stopListening(token: String) {
            listener = null
        }

        override suspend fun fetchMessages(recipientId: String): List<TransportMessagePayload> = emptyList()

        override suspend fun sendMessage(
            recipientId: String,
            documentId: String,
            payload: TransportMessagePayload,
        ): String = documentId

        override suspend fun deleteMessage(
            recipientId: String,
            documentId: String,
        ) {
            deletedIds += documentId
            if (failDeletes) {
                error("delete failed")
            }
        }

        override suspend fun ensureConversation(conversationId: String) {
        }

        fun emit(payload: TransportMessagePayload) {
            listener?.onMessages(listOf(payload))
        }
    }

    private class RecordingStorageBridge(
        private val downloadUrl: String,
    ) : MediaStorageBridge {
        val uploadedPaths = mutableListOf<String>()

        override suspend fun uploadBytes(
            objectPath: String,
            bytes: ByteArray,
        ): String {
            uploadedPaths += objectPath
            return downloadUrl
        }

        override suspend fun downloadBytes(
            remoteUrl: String,
            maxBytes: Long,
        ): ByteArray = ByteArray(0)

        override suspend fun deleteRemote(remoteUrl: String) {
        }
    }

    private class FakeSessionProvider(private val userId: String) : UserSessionProvider {
        override val session: Flow<UserSession?> = emptyFlow()

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = null

        override fun currentUserId(): String? = userId

        override fun currentUserPhone(): String? = null
    }

    private class FailingStorageBridge : MediaStorageBridge {
        var uploaded = false

        override suspend fun uploadBytes(
            objectPath: String,
            bytes: ByteArray,
        ): String {
            uploaded = true
            error("upload failed")
        }

        override suspend fun downloadBytes(
            remoteUrl: String,
            maxBytes: Long,
        ): ByteArray = ByteArray(0)

        override suspend fun deleteRemote(remoteUrl: String) {
        }
    }
}
