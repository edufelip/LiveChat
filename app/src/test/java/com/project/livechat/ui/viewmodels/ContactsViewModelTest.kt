package com.project.livechat.ui.viewmodels

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.project.livechat.data.repositories.mock.ContactsRepositoryMock
import com.project.livechat.data.repositories.mock.ContactsRepositoryMock.Companion.FAKE_LOCAL_CONTACT_ONE
import com.project.livechat.data.repositories.mock.ContactsRepositoryMock.Companion.FAKE_REGISTERED_CONTACT_ONE
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.utils.InstantTaskExecutorRule
import com.project.livechat.utils.MainCoroutineRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactsViewModelTest {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var contactsRepositoryMock: ContactsRepositoryMock
    private lateinit var checkRegisteredContactsUseCase: CheckRegisteredContactsUseCase
    private lateinit var getLocalContactsUseCase: GetLocalContactsUseCase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        contactsRepositoryMock = ContactsRepositoryMock()
        checkRegisteredContactsUseCase = CheckRegisteredContactsUseCase(contactsRepositoryMock)
        getLocalContactsUseCase = GetLocalContactsUseCase(contactsRepositoryMock)
        viewModel = ContactsViewModel(checkRegisteredContactsUseCase, getLocalContactsUseCase)
    }

    @Test
    fun `Should check validated contacts`() {
        runTest {
            viewModel.validatedContactsList.test {
                viewModel.checkContacts(
                    listOf(FAKE_LOCAL_CONTACT_ONE)
                )
                assertThat(awaitItem()).isEqualTo(StateUI.Idle)
                assertThat(awaitItem()).isEqualTo(StateUI.Loading)
                assertThat(awaitItem()).isEqualTo(StateUI.Success(emptyList<Contact>()))
            }
        }
    }

    @Test
    fun `Should add registered user to local`() {
        runTest {
            viewModel.localContactsList.test {
                viewModel.checkContacts(listOf(FAKE_REGISTERED_CONTACT_ONE))
                assertThat(awaitItem()).isEqualTo(StateUI.Idle)
                assertThat(awaitItem()).isEqualTo(StateUI.Loading)
                assertThat(awaitItem()).isEqualTo(StateUI.Success(listOf(FAKE_REGISTERED_CONTACT_ONE)))
            }
        }
    }

    @Test
    fun `Should delete contacts from local`() {

    }

    @Test
    fun `Should NOT add unregistered contact to local`() {

    }

    @Test
    fun `Should `() {

    }
}