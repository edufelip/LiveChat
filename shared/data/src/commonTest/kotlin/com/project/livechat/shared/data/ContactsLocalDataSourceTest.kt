package com.project.livechat.shared.data

import com.project.livechat.data.local.ContactsLocalDataSource
import com.project.livechat.domain.models.Contact
import com.project.livechat.shared.data.database.LiveChatDatabase
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
        val driver = createTestSqlDriver()
        val database = LiveChatDatabase(driver)
        val dataSource = ContactsLocalDataSource(database, dispatcher = StandardTestDispatcher(testScheduler))
        val contact = Contact(
            id = 0,
            name = "Alice",
            phoneNo = "+123456789",
            description = "Test user",
            photo = null
        )

        dataSource.addContact(contact)

        val stored = database.contactsQueries.getAllContacts().executeAsList()
        assertEquals(1, stored.size)
        assertEquals(contact.name, stored.first().name)
        assertEquals(contact.phoneNo, stored.first().phone_no)

        driver.close()
    }

    @Test
    fun updateContactWritesNewValues() = runTest {
        val driver = createTestSqlDriver()
        val database = LiveChatDatabase(driver)
        val dataSource = ContactsLocalDataSource(database, dispatcher = StandardTestDispatcher(testScheduler))
        val original = Contact(0, "Bob", "+198765432", description = "Original", photo = null)
        dataSource.addContact(original)

        val updated = original.copy(name = "Bob Updated", description = "Updated")
        dataSource.updateContact(listOf(updated))

        val stored = database.contactsQueries.getAllContacts().executeAsList()
        assertEquals("Bob Updated", stored.first().name)
        assertEquals("Updated", stored.first().description)

        driver.close()
    }

    @Test
    fun removeContactsClearsRows() = runTest {
        val driver = createTestSqlDriver()
        val database = LiveChatDatabase(driver)
        val dataSource = ContactsLocalDataSource(database, dispatcher = StandardTestDispatcher(testScheduler))
        val first = Contact(0, "Carol", "+12125551212", description = null, photo = null)
        val second = Contact(0, "Dave", "+13125551212", description = null, photo = null)
        dataSource.addContact(first)
        dataSource.addContact(second)

        dataSource.removeContacts(listOf(first, second))

        val stored = database.contactsQueries.getAllContacts().executeAsList()
        assertTrue(stored.isEmpty())

        driver.close()
    }
}
