package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.local.ContactsLocalDataSource
import com.edufelip.livechat.domain.models.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsLocalDataSourceTest {
    @Test
    fun addContactPersistsRows() = runTest {
        val database = createIosTestDatabase()
        val dataSource = ContactsLocalDataSource(database, dispatcher = StandardTestDispatcher(testScheduler))
        val contact =
            Contact(
                id = 0,
                name = "Alice",
                phoneNo = "+123456789",
                description = "Test user",
                photo = null,
            )

        dataSource.addContacts(listOf(contact))

        val stored = database.contactsDao().getAll()
        assertEquals(1, stored.size)
        assertEquals(contact.name, stored.first().name)
        assertEquals(contact.phoneNo, stored.first().phoneNo)

        database.close()
    }

    @Test
    fun removeContactsClearsRows() = runTest {
        val database = createIosTestDatabase()
        val dataSource = ContactsLocalDataSource(database, dispatcher = StandardTestDispatcher(testScheduler))
        val first = Contact(0, "Carol", "+12125551212", description = null, photo = null)
        val second = Contact(0, "Dave", "+13125551212", description = null, photo = null)
        dataSource.addContacts(listOf(first, second))

        dataSource.removeContacts(listOf(first, second))

        val stored = database.contactsDao().getAll()
        assertTrue(stored.isEmpty())

        database.close()
    }
}
