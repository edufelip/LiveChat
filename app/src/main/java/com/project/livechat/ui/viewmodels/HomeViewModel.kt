package com.project.livechat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.ui.screens.home.models.ConversationItemUiModel
import com.project.livechat.ui.screens.home.models.HomeUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val presenter: ConversationListPresenter
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    val uiState = presenter.uiState
        .map { state ->
            HomeUiState(
                conversations = state.conversations.map { it.toUiModel() },
                searchQuery = state.searchQuery,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun onSearchQueryChange(query: String) {
        presenter.setSearchQuery(query)
    }

    fun onConversationOpened(conversationId: String) {
        presenter.markConversationAsRead(conversationId)
    }

    fun togglePinned(conversationId: String, pinned: Boolean) {
        presenter.togglePinned(conversationId, pinned)
    }

    fun clearError() {
        presenter.clearError()
    }

    private fun ConversationSummary.toUiModel(): ConversationItemUiModel {
        val instant = Instant.ofEpochMilli(lastMessage.createdAt)
        val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
        val label = if (localDateTime.toLocalDate() == LocalDate.now(zoneId)) {
            localDateTime.format(timeFormatter)
        } else {
            localDateTime.format(dateFormatter)
        }
        val preview = lastMessage.body.takeUnless { it.isBlank() }?.let { body ->
            if (body.length > 140) body.take(137) + "…" else body
        } ?: ""

        return ConversationItemUiModel(
            id = conversationId,
            title = displayName,
            subtitle = preview,
            timeLabel = label,
            timestamp = lastMessage.createdAt,
            unreadCount = unreadCount,
            isPinned = isPinned,
            avatarUrl = contactPhoto
        )
    }

    override fun onCleared() {
        presenter.close()
        super.onCleared()
    }
}
