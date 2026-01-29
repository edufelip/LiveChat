package com.edufelip.livechat.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationListUiState(
    val conversations: List<ConversationSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: ConversationFilter = ConversationFilter.All,
    val currentUserId: String? = null,
)

enum class ConversationFilter(val displayName: String) {
    All("All"),
    Unread("Unread"),
    Pinned("Pinned"),
    Archived("Archived"),
}
