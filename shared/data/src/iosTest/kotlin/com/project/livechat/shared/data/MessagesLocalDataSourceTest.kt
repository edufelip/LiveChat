package com.project.livechat.shared.data

import com.project.livechat.data.local.MessagesLocalDataSource
import com.project.livechat.data.mappers.toDomain
import com.project.livechat.domain.models.AttachmentRef
import com.project.livechat.domain.models.CipherInfo
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageContentType
import com.project.livechat.domain.models.MessageStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesLocalDataSourceTest {
    @Test
    fun insertOutgoingMessagePersistsExtendedFields() = runTest {
        val database = createIosTestDatabase()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dataSource = MessagesLocalDataSource(database, dispatcher = dispatcher)
        val message =
            Message(
                id = "local-1",
                conversationId = "conversation-1",
                senderId = "user-123",
                body = "Encrypted payload",
                createdAt = 1000L,
                status = MessageStatus.SENDING,
                localTempId = "local-1",
                messageSeq = 42L,
                serverAckAt = 1010L,
                contentType = MessageContentType.Encrypted,
                ciphertext = "cipher-text",
                replyToMessageId = "local-0",
                threadRootId = "root-thread",
                editedAt = 1020L,
                attachments =
                    listOf(
                        AttachmentRef(
                            objectKey = "object-key",
                            mimeType = "image/png",
                            sizeBytes = 2048,
                            thumbnailKey = "thumb-key",
                            cipherInfo =
                                CipherInfo(
                                    algorithm = "AES-GCM",
                                    keyId = "key-1",
                                    nonce = "nonce",
                                    associatedData = "assoc",
                                ),
                        ),
                    ),
                metadata = mapOf("channel" to "sms"),
            )

        dataSource.insertOutgoingMessage(message)

        val storedRow = database.messagesDao().getMessages("conversation-1").first()
        assertEquals(42L, storedRow.messageSeq)
        assertEquals("Encrypted", storedRow.contentType)
        assertEquals("cipher-text", storedRow.ciphertext)
        assertEquals("local-0", storedRow.replyToMessageId)
        assertEquals("root-thread", storedRow.threadRootId)
        assertEquals(1020L, storedRow.editedAt)

        val domain = storedRow.toDomain()
        assertEquals(MessageContentType.Encrypted, domain.contentType)
        assertEquals(42L, domain.messageSeq)
        assertEquals("cipher-text", domain.ciphertext)
        assertEquals("local-0", domain.replyToMessageId)
        assertEquals("root-thread", domain.threadRootId)
        assertEquals(1, domain.attachments.size)
        assertEquals("sms", domain.metadata["channel"])

        database.close()
    }
}
