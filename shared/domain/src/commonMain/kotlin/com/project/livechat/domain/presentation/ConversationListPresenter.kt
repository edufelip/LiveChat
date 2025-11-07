package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.ConversationFilter
import com.project.livechat.domain.models.ConversationListUiState
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.useCases.MarkConversationReadUseCase
import com.project.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.project.livechat.domain.useCases.SetConversationArchivedUseCase
import com.project.livechat.domain.useCases.SetConversationMutedUseCase
import com.project.livechat.domain.useCases.SetConversationPinnedUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import com.project.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationListPresenter(
    private val observeConversationSummaries: ObserveConversationSummariesUseCase,
    private val markConversationRead: MarkConversationReadUseCase,
    private val setConversationPinned: SetConversationPinnedUseCase,
    private val setConversationMuted: SetConversationMutedUseCase,
    private val setConversationArchived: SetConversationArchivedUseCase,
    private val scope: CoroutineScope = MainScope(),
) {
    private val _uiState = MutableStateFlow(ConversationListUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    val cState: CStateFlow<ConversationListUiState> = uiState.asCStateFlow()

    private var cachedSummaries: List<ConversationSummary> = emptyList()

    init {
        scope.launch {
            observeConversationSummaries()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                }
                .collectLatest { summaries ->
                    cachedSummaries = summaries
                    _uiState.update { state ->
                        state.copy(
                            conversations = filterSummaries(state.searchQuery, state.selectedFilter, summaries),
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val trimmed = query.trim()
            state.copy(
                searchQuery = trimmed,
                conversations = filterSummaries(trimmed, state.selectedFilter, cachedSummaries),
            )
        }
    }

    fun setFilter(filter: ConversationFilter) {
        _uiState.update { state ->
            if (state.selectedFilter == filter) {
                state
            } else {
                state.copy(
                    selectedFilter = filter,
                    conversations = filterSummaries(state.searchQuery, filter, cachedSummaries),
                )
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        val summary = cachedSummaries.find { it.conversationId == conversationId } ?: return
        val lastReadAt = summary.lastMessage.createdAt
        val lastReadSeq = summary.lastMessage.messageSeq
        scope.launch {
            runCatching {
                markConversationRead(conversationId, lastReadAt, lastReadSeq)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun togglePinned(
        conversationId: String,
        pinned: Boolean,
    ) {
        val timestamp = if (pinned) currentEpochMillis() else null
        scope.launch {
            runCatching {
                setConversationPinned(conversationId, pinned, timestamp)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun toggleMuted(
        conversationId: String,
        muted: Boolean,
    ) {
        val muteUntil = if (muted) currentEpochMillis() + DEFAULT_MUTE_DURATION_MS else null
        scope.launch {
            runCatching {
                setConversationMuted(conversationId, muteUntil)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun toggleArchived(
        conversationId: String,
        archived: Boolean,
    ) {
        scope.launch {
            runCatching {
                setConversationArchived(conversationId, archived)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    private fun filterSummaries(
        query: String,
        filter: ConversationFilter,
        items: List<ConversationSummary>,
    ): List<ConversationSummary> {
        val (archived, active) = items.partition { it.isArchived }
        val filtered =
            when (filter) {
                ConversationFilter.All -> active
                ConversationFilter.Unread -> active.filter { it.unreadCount > 0 }
                ConversationFilter.Pinned -> active.filter { it.isPinned }
                ConversationFilter.Archived -> archived
            }
        if (query.isBlank()) return filtered.sortedWith(summaryComparator)
        val lower = query.lowercase()
        return filtered.filter {
            it.displayName.lowercase().contains(lower) ||
                it.lastMessage.body.lowercase().contains(lower)
        }.sortedWith(summaryComparator)
    }

    private val summaryComparator =
        Comparator<ConversationSummary> { a, b ->
            when {
                a.isPinned && !b.isPinned -> -1
                !a.isPinned && b.isPinned -> 1
                else -> b.lastMessage.createdAt.compareTo(a.lastMessage.createdAt)
            }
        }

    companion object {
        private const val DEFAULT_MUTE_DURATION_MS = 7L * 24 * 60 * 60 * 1000 // 7 days
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }
}
