package com.project.livechat.domain.models

data class ContactsUiState(
    val localContacts: List<Contact> = emptyList(),
    val validatedContacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)
