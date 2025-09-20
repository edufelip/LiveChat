package com.project.livechat.ui.screens.home.models

data class ConversationItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val timestamp: Long,
    val unreadCount: Int,
    val isPinned: Boolean,
    val avatarUrl: String?
)

data class HomeUiState(
    val conversations: List<ConversationItemUiModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
