package com.edufelip.livechat.domain.utils

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState

object ContactsUiStateCache {
    private var cachedState: ContactsUiState? = null
    private var hasVisited: Boolean = false

    fun snapshot(): ContactsUiState? = cachedState

    fun hasVisitedContacts(): Boolean = hasVisited

    fun markVisited() {
        requireMainThread("ContactsUiStateCache.markVisited")
        hasVisited = true
    }

    fun seedFromSnapshot(contacts: List<Contact>) {
        requireMainThread("ContactsUiStateCache.seedFromSnapshot")
        if (cachedState != null || contacts.isEmpty()) return
        cachedState =
            ContactsUiState(
                localContacts = contacts,
                validatedContacts = contacts.filter { it.isRegistered },
                isLoading = false,
                isSyncing = false,
                errorMessage = null,
            )
    }

    fun update(state: ContactsUiState) {
        requireMainThread("ContactsUiStateCache.update")
        cachedState = state.copy(isLoading = false, errorMessage = null)
    }

    fun clear() {
        requireMainThread("ContactsUiStateCache.clear")
        cachedState = null
        hasVisited = false
    }
}
