package com.edufelip.livechat.shared.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.contracts.IPresenceRemoteData
import com.edufelip.livechat.data.models.InboxAction
import com.edufelip.livechat.data.models.InboxItem
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.PresenceState
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.koin.core.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class KoinInitializationTest {
    private var koinApplication: KoinApplication? = null

    @AfterTest
    fun tearDown() {
        koinApplication?.close()
        stopKoin()
    }

    @Test
    fun initSharedKoinProvidesDependencies() =
        runTest {
            val database = createTestDatabase()
            val platformModule =
                module {
                    single { database }
                    single<UserSessionProvider> { StubUserSessionProvider }
                }

            val backendModule =
                module {
                    single<IAccountRemoteData> { StubAccountRemoteData }
                    single<IContactsRemoteData> { StubContactsRemoteData }
                    single<IMessagesRemoteData> { StubMessagesRemoteData }
                    single<IPresenceRemoteData> { StubPresenceRemoteData }
                }

            koinApplication =
                initSharedKoin(
                    platformModules = listOf(platformModule),
                    backendModules = listOf(backendModule),
                )

            val koin = koinApplication!!.koin
            assertNotNull(koin.get<LiveChatDatabase>())
            assertNotNull(koin.get<IContactsRepository>())

            database.close()
        }

    private object StubContactsRemoteData : IContactsRemoteData {
        override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> = emptyFlow()

        override suspend fun inviteContact(contact: Contact): Boolean = true
    }

    private object StubAccountRemoteData : IAccountRemoteData {
        override suspend fun fetchAccountProfile(
            userId: String,
            idToken: String,
        ) = null

        override suspend fun updateDisplayName(
            userId: String,
            idToken: String,
            displayName: String,
        ) = Unit

        override suspend fun updateStatusMessage(
            userId: String,
            idToken: String,
            statusMessage: String,
        ) = Unit

        override suspend fun updateEmail(
            userId: String,
            idToken: String,
            email: String,
        ) = Unit

        override suspend fun ensureUserDocument(
            userId: String,
            idToken: String,
            phoneNumber: String?,
        ) = Unit

        override suspend fun deleteAccount(
            userId: String,
            idToken: String,
        ) = Unit
    }

    private object StubMessagesRemoteData : IMessagesRemoteData {
        override fun observeConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): Flow<List<InboxItem>> = flowOf(emptyList())

        override suspend fun sendMessage(draft: MessageDraft): Message =
            Message(
                id = draft.localId,
                conversationId = draft.conversationId,
                senderId = draft.senderId,
                body = draft.body,
                createdAt = draft.createdAt,
                status = MessageStatus.SENT,
            )

        override suspend fun pullHistorical(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

        override suspend fun downloadMediaToLocal(
            remoteUrl: String,
            contentType: com.edufelip.livechat.domain.models.MessageContentType,
        ): String = ""

        override suspend fun deleteMedia(remoteUrl: String) = Unit

        override suspend fun sendAction(action: InboxAction) = Unit

        override suspend fun ensureConversation(
            conversationId: String,
            userId: String,
            userPhone: String?,
            peer: ConversationPeer?,
        ) = Unit
    }

    private object StubPresenceRemoteData : IPresenceRemoteData {
        override suspend fun fetchPresence(
            userIds: List<String>,
            idToken: String,
        ): Map<String, PresenceState> = emptyMap()

        override suspend fun updatePresence(
            userId: String,
            idToken: String,
            isOnline: Boolean,
            lastActiveAt: Long,
        ) = Unit
    }

    private object StubUserSessionProvider : UserSessionProvider {
        override val session: Flow<UserSession?> = emptyFlow()

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = null

        override fun currentUserId(): String? = null

        override fun currentUserPhone(): String? = null
    }
}
