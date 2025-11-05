package com.project.livechat.composeapp.preview

import androidx.compose.ui.graphics.Color
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.models.ConversationListUiState
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.models.ConversationUiState
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageStatus

object PreviewFixtures {
    private const val TIMESTAMP = 1_696_000_000_000L

    val sampleMessages: List<Message> =
        listOf(
            Message(
                id = "1",
                conversationId = "conversation-1",
                senderId = "preview-user",
                body = "Hey there! This is a preview message to show how your own messages appear in bubbles.",
                createdAt = TIMESTAMP,
                status = MessageStatus.DELIVERED,
            ),
            Message(
                id = "2",
                conversationId = "conversation-1",
                senderId = "friend",
                body = "Hi 👋🏾 Compose Multiplatform previews are working like a charm!",
                createdAt = TIMESTAMP - 90_000,
                status = MessageStatus.SENT,
            ),
        )

    val conversationUiState =
        ConversationUiState(
            conversationId = "conversation-1",
            messages = sampleMessages,
            isLoading = false,
            isSending = false,
            errorMessage = null,
        )

    val loadingConversationState =
        ConversationUiState(
            conversationId = "conversation-1",
            messages = emptyList(),
            isLoading = true,
            isSending = false,
            errorMessage = null,
        )

    val sampleConversations: List<ConversationSummary> =
        listOf(
            ConversationSummary(
                conversationId = "conversation-1",
                contactName = "Ava Harper",
                contactPhoto = null,
                lastMessage = sampleMessages.first(),
                unreadCount = 3,
                isPinned = true,
                pinnedAt = TIMESTAMP,
                lastReadAt = TIMESTAMP - 5_000,
            ),
            ConversationSummary(
                conversationId = "conversation-2",
                contactName = "Brandon Diaz",
                contactPhoto = null,
                lastMessage = sampleMessages.last().copy(body = "Let's schedule a quick catch-up later today."),
                unreadCount = 0,
                isPinned = false,
                pinnedAt = null,
                lastReadAt = TIMESTAMP - 20_000,
            ),
        )

    val conversationListState =
        ConversationListUiState(
            conversations = sampleConversations,
            searchQuery = "",
            isLoading = false,
            errorMessage = null,
        )

    val conversationListLoading = conversationListState.copy(conversations = emptyList(), isLoading = true)
    val conversationListEmpty = conversationListState.copy(conversations = emptyList(), isLoading = false)
    val conversationListError = conversationListState.copy(errorMessage = "Something went wrong")

    val contacts: List<Contact> =
        listOf(
            Contact(id = 1, name = "Ava Harper", phoneNo = "+1 555 0100", description = "Designer", photo = null),
            Contact(id = 2, name = "Brandon Diaz", phoneNo = "+1 555 0101", description = "Product Manager", photo = null),
            Contact(id = 3, name = "Chioma Ade", phoneNo = "+1 555 0102", description = "iOS Engineer", photo = null),
        )

    val contactsState =
        ContactsUiState(
            localContacts = contacts,
            validatedContacts = contacts.take(2),
            isLoading = false,
            isSyncing = false,
            errorMessage = null,
        )

    val contactsLoadingState = contactsState.copy(localContacts = emptyList(), validatedContacts = emptyList(), isLoading = true)
    val contactsErrorState = contactsState.copy(errorMessage = "Invite failed")

    val badgeColors = listOf(Color(0xFF2F9D62), Color(0xFF635BFF))
}
