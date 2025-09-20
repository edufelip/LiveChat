package com.project.livechat.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.utils.CStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContactsViewModel(
    private val presenter: ContactsPresenter
) : ViewModel() {

    val uiState: StateFlow<ContactsUiState> = presenter.state
    val cState: CStateFlow<ContactsUiState> = presenter.cState

    fun syncContacts(phoneContacts: List<Contact>) {
        presenter.syncContacts(phoneContacts)
    }

    fun invite(contact: Contact) {
        presenter.inviteContact(contact)
    }

    fun clearError() {
        presenter.clearError()
    }

    override fun onCleared() {
        presenter.close()
        super.onCleared()
    }
}
