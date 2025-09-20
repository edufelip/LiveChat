package com.project.livechat.shared.data

import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.data.contracts.IMessagesRemoteData
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.providers.model.UserSession
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.initSharedKoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.koin.core.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinInitializationTest {

    private var koinApplication: KoinApplication? = null

    @AfterTest
    fun tearDown() {
        koinApplication?.close()
        stopKoin()
    }

    @Test
    fun initSharedKoinProvidesDependencies() = runTest {
        val driver = createTestSqlDriver()
        val platformModule = module {
            single { driver }
            single<UserSessionProvider> { StubUserSessionProvider }
        }

        val backendModule = module {
            single<IContactsRemoteData> { StubContactsRemoteData }
            single<IMessagesRemoteData> { StubMessagesRemoteData }
        }

        koinApplication = initSharedKoin(
            platformModules = listOf(platformModule),
            backendModules = listOf(backendModule)
        )

        val koin = koinApplication!!.koin
        assertNotNull(koin.get<LiveChatDatabase>())
        assertNotNull(koin.get<IContactsRepository>())

        driver.close()
    }

    private object StubContactsRemoteData : IContactsRemoteData {
        override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> = emptyFlow()
    }

    private object StubMessagesRemoteData : IMessagesRemoteData {
        override fun observeConversation(conversationId: String, sinceEpochMillis: Long?): Flow<List<Message>> =
            flowOf(emptyList())

        override suspend fun sendMessage(draft: MessageDraft): Message =
            Message(
                id = draft.localId,
                conversationId = draft.conversationId,
                senderId = draft.senderId,
                body = draft.body,
                createdAt = draft.createdAt,
                status = MessageStatus.SENT
            )

        override suspend fun pullHistorical(conversationId: String, sinceEpochMillis: Long?): List<Message> = emptyList()
    }

    private object StubUserSessionProvider : UserSessionProvider {
        override val session: Flow<UserSession?> = emptyFlow()

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = null

        override fun currentUserId(): String? = null
    }
}
