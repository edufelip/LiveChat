package com.edufelip.livechat.preview

import androidx.compose.ui.graphics.Color
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.models.ParticipantRole
import com.edufelip.livechat.ui.resources.LiveChatStrings

object PreviewFixtures {
    private const val TIMESTAMP = 1_696_000_000_000L
    private const val PREVIEW_USER_ID = "preview-user"
    private const val PREVIEW_FRIEND_ID = "preview-friend"
    private const val CONVERSATION_PRIMARY_ID = "conversation-1"
    private const val CONVERSATION_SECONDARY_ID = "conversation-2"

    val badgeColors = listOf(Color(0xFF64C7A8), Color(0xFF82D4B8))

    fun previewUserId(): String = PREVIEW_USER_ID

    fun sampleMessages(strings: LiveChatStrings): List<Message> {
        val preview = strings.preview
        return listOf(
            Message(
                id = "1",
                conversationId = CONVERSATION_PRIMARY_ID,
                senderId = PREVIEW_USER_ID,
                body = preview.messageOutgoing,
                createdAt = TIMESTAMP,
                status = MessageStatus.DELIVERED,
            ),
            Message(
                id = "2",
                conversationId = CONVERSATION_PRIMARY_ID,
                senderId = PREVIEW_FRIEND_ID,
                body = preview.messageIncoming,
                createdAt = TIMESTAMP - 90_000,
                status = MessageStatus.SENT,
            ),
        )
    }

    fun conversationUiState(strings: LiveChatStrings): ConversationUiState {
        val messages = sampleMessages(strings)
        return ConversationUiState(
            conversationId = CONVERSATION_PRIMARY_ID,
            messages = messages,
            isLoading = false,
            isSending = false,
            errorMessage = null,
            participant =
                Participant(
                    conversationId = CONVERSATION_PRIMARY_ID,
                    userId = PREVIEW_USER_ID,
                    role = ParticipantRole.Member,
                    joinedAt = TIMESTAMP - 10_000,
                    muteUntil = TIMESTAMP + 3_600_000,
                    pinned = true,
                    pinnedAt = TIMESTAMP,
                    archived = false,
                ),
            isMuted = true,
            muteUntil = TIMESTAMP + 3_600_000,
            isArchived = false,
        )
    }

    fun loadingConversationState(): ConversationUiState =
        ConversationUiState(
            conversationId = CONVERSATION_PRIMARY_ID,
            messages = emptyList(),
            isLoading = true,
            isSending = false,
            errorMessage = null,
        )

    fun conversationListState(strings: LiveChatStrings): ConversationListUiState {
        val preview = strings.preview
        val messages = sampleMessages(strings)
        val sampleConversations =
            listOf(
                ConversationSummary(
                    conversationId = CONVERSATION_PRIMARY_ID,
                    contactName = preview.contactPrimaryName,
                    contactPhoto = null,
                    lastMessage = messages.first(),
                    unreadCount = 3,
                    isPinned = true,
                    pinnedAt = TIMESTAMP,
                    lastReadAt = TIMESTAMP - 5_000,
                    isOnline = true,
                ),
                ConversationSummary(
                    conversationId = CONVERSATION_SECONDARY_ID,
                    contactName = preview.contactSecondaryName,
                    contactPhoto = null,
                    lastMessage = messages.last().copy(body = preview.messageSnippetSecondary),
                    unreadCount = 0,
                    isPinned = false,
                    pinnedAt = null,
                    lastReadAt = TIMESTAMP - 20_000,
                ),
            )
        return ConversationListUiState(
            conversations = sampleConversations,
            searchQuery = "",
            isLoading = false,
            errorMessage = null,
            selectedFilter = ConversationFilter.All,
            currentUserId = PREVIEW_USER_ID,
        )
    }

    fun conversationListLoading(strings: LiveChatStrings): ConversationListUiState =
        conversationListState(strings).copy(conversations = emptyList(), isLoading = true)

    fun conversationListEmpty(strings: LiveChatStrings): ConversationListUiState =
        conversationListState(strings).copy(conversations = emptyList(), isLoading = false)

    fun conversationListError(strings: LiveChatStrings): ConversationListUiState =
        conversationListState(strings).copy(errorMessage = strings.general.errorTitle)

    fun contacts(strings: LiveChatStrings): List<Contact> {
        val preview = strings.preview
        return listOf(
            Contact(
                id = 1,
                name = preview.contactPrimaryName,
                phoneNo = preview.contactPrimaryPhone,
                description = null,
                photo = null,
                isRegistered = true,
            ),
            Contact(
                id = 2,
                name = preview.contactSecondaryName,
                phoneNo = preview.contactSecondaryPhone,
                description = null,
                photo = null,
                isRegistered = true,
            ),
            Contact(
                id = 3,
                name = preview.contactTertiaryName,
                phoneNo = preview.contactTertiaryPhone,
                description = null,
                photo = null,
                isRegistered = false,
            ),
        )
    }

    fun contactsState(strings: LiveChatStrings): ContactsUiState {
        val previewContacts = contacts(strings)
        return ContactsUiState(
            localContacts = previewContacts,
            validatedContacts = previewContacts.filter { it.isRegistered },
            isLoading = false,
            isSyncing = false,
            errorMessage = null,
        )
    }

    fun contactsLoadingState(strings: LiveChatStrings): ContactsUiState =
        contactsState(strings).copy(localContacts = emptyList(), validatedContacts = emptyList(), isLoading = true)

    fun contactsErrorState(strings: LiveChatStrings): ContactsUiState =
        contactsState(strings).copy(errorMessage = strings.general.errorTitle)
}
