package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.ConversationListUiState
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.useCases.MarkConversationReadUseCase
import com.project.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.project.livechat.domain.useCases.SetConversationPinnedUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ConversationListPresenter(
    private val observeConversationSummaries: ObserveConversationSummariesUseCase,
    private val markConversationRead: MarkConversationReadUseCase,
    private val setConversationPinned: SetConversationPinnedUseCase,
    private val scope: CoroutineScope = MainScope()
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
                            conversations = filterSummaries(state.searchQuery, summaries),
                            isLoading = false,
                            errorMessage = null
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
                conversations = filterSummaries(trimmed, cachedSummaries)
            )
        }
    }

    fun markConversationAsRead(conversationId: String) {
        val summary = cachedSummaries.find { it.conversationId == conversationId } ?: return
        val lastReadAt = summary.lastMessage.createdAt
        scope.launch {
            runCatching {
                markConversationRead(conversationId, lastReadAt)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    fun togglePinned(conversationId: String, pinned: Boolean) {
        val timestamp = if (pinned) Clock.System.now().toEpochMilliseconds() else null
        scope.launch {
            runCatching {
                setConversationPinned(conversationId, pinned, timestamp)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    private fun filterSummaries(query: String, items: List<ConversationSummary>): List<ConversationSummary> {
        if (query.isBlank()) return items.sortedWith(summaryComparator)
        val lower = query.lowercase()
        return items.filter {
            it.displayName.lowercase().contains(lower) ||
                it.lastMessage.body.lowercase().contains(lower)
        }.sortedWith(summaryComparator)
    }

    private val summaryComparator = Comparator<ConversationSummary> { a, b ->
        when {
            a.isPinned && !b.isPinned -> -1
            !a.isPinned && b.isPinned -> 1
            else -> b.lastMessage.createdAt.compareTo(a.lastMessage.createdAt)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        scope.cancel()
    }
}
