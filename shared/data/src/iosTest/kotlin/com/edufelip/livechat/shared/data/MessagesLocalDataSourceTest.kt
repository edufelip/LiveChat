package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.local.MessagesLocalDataSource
import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.domain.models.AttachmentRef
import com.edufelip.livechat.domain.models.CipherInfo
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesLocalDataSourceTest {
    @Test
    fun insertOutgoingMessagePersistsExtendedFields() =
        runTest {
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

    @Test
    fun latestIncomingMessageSkipsOwnMessages_andTracksActionProcessing() =
        runTest {
            val database = createIosTestDatabase()
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dataSource = MessagesLocalDataSource(database, dispatcher = dispatcher)
            val now = currentEpochMillis()
            val outgoing =
                Message(
                    id = "local-out",
                    conversationId = "conversation-2",
                    senderId = "user-self",
                    body = "outgoing",
                    createdAt = now - 1_000L,
                    status = MessageStatus.SENT,
                )
            val incoming =
                Message(
                    id = "remote-in",
                    conversationId = "conversation-2",
                    senderId = "user-peer",
                    body = "incoming",
                    createdAt = now,
                    status = MessageStatus.SENT,
                )

            dataSource.upsertMessages(listOf(outgoing, incoming))

            val latestIncoming = dataSource.latestIncomingMessage("conversation-2", "user-self")
            assertNotNull(latestIncoming)
            assertEquals("remote-in", latestIncoming.id)

            assertFalse(dataSource.hasProcessedAction("action-1"))
            dataSource.markActionProcessed("action-1")
            assertTrue(dataSource.hasProcessedAction("action-1"))

            database.close()
        }

    @Test
    fun processedActionsPersistAcrossDatabaseRestart() =
        runTest {
            val path = platform.Foundation.NSTemporaryDirectory() + "/livechat-persist-" + platform.Foundation.NSUUID().UUIDString + ".db"
            val database =
                com.edufelip.livechat.shared.data.database.buildLiveChatDatabase(
                    androidx.room.Room.databaseBuilder<com.edufelip.livechat.shared.data.database.LiveChatDatabase>(
                        name = path,
                    ),
                )
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dataSource = MessagesLocalDataSource(database, dispatcher = dispatcher)

            dataSource.markActionProcessed("action-persist")
            assertTrue(dataSource.hasProcessedAction("action-persist"))

            database.close()

            val reopened =
                com.edufelip.livechat.shared.data.database.buildLiveChatDatabase(
                    androidx.room.Room.databaseBuilder<com.edufelip.livechat.shared.data.database.LiveChatDatabase>(
                        name = path,
                    ),
                )
            val reopenedDataSource = MessagesLocalDataSource(reopened, dispatcher = dispatcher)
            assertTrue(reopenedDataSource.hasProcessedAction("action-persist"))

            reopened.close()
        }
}
