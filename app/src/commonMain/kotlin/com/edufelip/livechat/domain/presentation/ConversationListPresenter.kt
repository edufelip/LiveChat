package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.PresenceState
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObservePresenceUseCase
import com.edufelip.livechat.domain.useCases.SetConversationArchivedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationMutedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationPinnedUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.ConversationListSnapshotCache
import com.edufelip.livechat.domain.utils.asCStateFlow
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationListPresenter(
    private val observeConversationSummaries: ObserveConversationSummariesUseCase,
    private val markConversationRead: MarkConversationReadUseCase,
    private val setConversationPinned: SetConversationPinnedUseCase,
    private val setConversationMuted: SetConversationMutedUseCase,
    private val setConversationArchived: SetConversationArchivedUseCase,
    private val observePresence: ObservePresenceUseCase,
    private val sessionProvider: UserSessionProvider,
    private val scope: CoroutineScope = MainScope(),
) {
    private val summaryComparator =
        Comparator<ConversationSummary> { a, b ->
            when {
                a.isPinned && !b.isPinned -> -1
                !a.isPinned && b.isPinned -> 1
                else -> b.lastMessage.createdAt.compareTo(a.lastMessage.createdAt)
            }
        }

    private val initialSummaries = ConversationListSnapshotCache.snapshot()
    private val _uiState =
        MutableStateFlow(
            if (initialSummaries == null) {
                ConversationListUiState(isLoading = true)
            } else {
                ConversationListUiState(
                    conversations = filterSummaries("", ConversationFilter.All, initialSummaries),
                    isLoading = false,
                    currentUserId = sessionProvider.currentUserId(),
                )
            },
        )
    val uiState = _uiState.asStateFlow()
    val cState: CStateFlow<ConversationListUiState> = uiState.asCStateFlow()

    private var cachedSummaries: List<ConversationSummary> = initialSummaries ?: emptyList()
    private var ignoreInitialEmptyEmission = initialSummaries?.isNotEmpty() == true
    private var cachedPresence: Map<String, PresenceState> = emptyMap()
    private val presenceTargets = MutableStateFlow<List<String>>(emptyList())

    init {
        scope.launch {
            observeConversationSummaries()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                }.collectLatest { summaries ->
                    if (ignoreInitialEmptyEmission && summaries.isEmpty()) {
                        ignoreInitialEmptyEmission = false
                        return@collectLatest
                    }
                    ignoreInitialEmptyEmission = false
                    cachedSummaries = summaries
                    ConversationListSnapshotCache.update(summaries)
                    presenceTargets.value = summaries.mapNotNull { presenceKey(it) }
                    _uiState.update { state ->
                        state.copy(
                            conversations = applyPresence(filterSummaries(state.searchQuery, state.selectedFilter, summaries)),
                            isLoading = false,
                            errorMessage = null,
                            currentUserId = sessionProvider.currentUserId(),
                        )
                    }
                }
        }
        scope.launch {
            presenceTargets
                .map { targets -> targets.distinct().filter { it.isNotBlank() } }
                .distinctUntilChanged()
                .flatMapLatest { targets -> observePresence(targets) }
                .collectLatest { presence ->
                    cachedPresence = presence
                    val state = _uiState.value
                    val filtered = filterSummaries(state.searchQuery, state.selectedFilter, cachedSummaries)
                    _uiState.update { current ->
                        current.copy(conversations = applyPresence(filtered))
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val trimmed = query.trim()
            state.copy(
                searchQuery = trimmed,
                conversations = applyPresence(filterSummaries(trimmed, state.selectedFilter, cachedSummaries)),
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
                    conversations = applyPresence(filterSummaries(state.searchQuery, filter, cachedSummaries)),
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
        return filtered
            .filter {
                it.displayName.lowercase().contains(lower) ||
                    it.lastMessage.body
                        .lowercase()
                        .contains(lower)
            }.sortedWith(summaryComparator)
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

    private fun applyPresence(summaries: List<ConversationSummary>): List<ConversationSummary> {
        if (summaries.isEmpty() || cachedPresence.isEmpty()) return summaries
        return summaries.map { summary ->
            val key = presenceKey(summary)
            val presence = key?.let { cachedPresence[it] }
            if (presence == null) {
                summary
            } else {
                summary.copy(isOnline = presence.isOnline)
            }
        }
    }

    private fun presenceKey(summary: ConversationSummary): String? {
        val candidate = summary.contactUserId?.takeIf { it.isNotBlank() } ?: summary.conversationId
        if (candidate.isBlank()) return null
        return if (candidate.isLikelyPhoneNumber()) null else candidate
    }

    private fun String.isLikelyPhoneNumber(): Boolean {
        if (isBlank()) return true
        if (any { it.isLetter() }) return false
        val digits = filter { it.isDigit() }
        if (digits.length < 7) return false
        val allowedSymbols = setOf('+', '(', ')', '-', ' ')
        return all { it.isDigit() || it in allowedSymbols }
    }
}
