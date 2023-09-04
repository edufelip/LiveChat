package com.project.livechat.data.db

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.project.livechat.data.db.entities.ContactRoom
import com.project.livechat.utils.InstantTaskExecutorRule
import com.project.livechat.utils.launchLatchTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class ContactDaoTest {
    private val defaultContact = ContactRoom(
        id = 1,
        name = "Contact",
        phoneNo = "+55121212",
        description = null,
        photo = null
    )

    private val supContact = ContactRoom(
        id = 2,
        name = "Example 1",
        phoneNo = "+55",
        description = null,
        photo = null
    )

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var database: ContactDatabase
    private lateinit var dao: ContactDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = database.contactDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun shouldGetAllContacts() = runTest {
        dao.insertContact(defaultContact)
        dao.insertContact(supContact)

        val latch = CountDownLatch(1)
        launchLatchTest(latch = latch) {
            dao.getAllContacts().collect { contactList ->
                assertThat(contactList.size).isEqualTo(2)
                assertThat(contactList[0]).isEqualTo(defaultContact)
                assertThat(contactList[1]).isEqualTo(supContact)
                latch.countDown()
            }
        }
    }

    @Test
    fun shouldInsertContact() = runTest {
        dao.insertContact(defaultContact)
        val latch = CountDownLatch(1)
        launchLatchTest(latch = latch) {
            dao.getAllContacts().collect {
                assertThat(it.size).isEqualTo(1)
                latch.countDown()
            }
        }
    }

    @Test
    fun shouldUpdateContact() = runTest {
        val newName = "New Name"
        val newDescription = "New Description"
        val newPhoto = "New Photo"

        dao.insertContact(defaultContact)
        dao.updateContact(
            name = newName,
            phoneNo = defaultContact.phoneNo,
            description = newDescription,
            photo = newPhoto
        )
        dao.getAllContacts().collect {
            it.first().let { contact ->
                assertThat(contact.name).isEqualTo(newName)
                assertThat(contact.phoneNo).isEqualTo(defaultContact.phoneNo)
                assertThat(contact.description).isEqualTo(newDescription)
                assertThat(contact.photo).isEqualTo(newPhoto)
            }
        }
    }

    @Test
    fun shouldDeleteContact() = runTest {
        dao.insertContact(defaultContact)
        dao.deleteContacts(listOf(defaultContact.phoneNo))
        dao.getAllContacts().collect {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun shouldDeleteAllContacts() = runTest {
        dao.insertContact(defaultContact)
        dao.insertContact(supContact)
        dao.deleteContacts(listOf(defaultContact.phoneNo, supContact.phoneNo))
        dao.getAllContacts().collect {
            assertThat(it).isEmpty()
        }
    }
}