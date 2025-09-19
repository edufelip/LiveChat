package com.project.livechat.shared.data

import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.initSharedKoin
import kotlinx.coroutines.flow.Flow
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
            single<IContactsRemoteData> { StubContactsRemoteData }
        }

        koinApplication = initSharedKoin(platformModules = listOf(platformModule))

        val koin = koinApplication!!.koin
        assertNotNull(koin.get<LiveChatDatabase>())
        assertNotNull(koin.get<IContactsRepository>())

        driver.close()
    }

    private object StubContactsRemoteData : IContactsRemoteData {
        override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> = emptyFlow()
    }
}
