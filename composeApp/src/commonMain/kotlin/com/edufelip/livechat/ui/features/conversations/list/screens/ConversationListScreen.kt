package com.edufelip.livechat.ui.features.conversations.list.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.components.atoms.SectionHeader
import com.edufelip.livechat.ui.components.molecules.EmptyState
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.features.conversations.list.components.ConversationListRow
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationListScreen(
    state: ConversationListUiState,
    onSearch: (String) -> Unit,
    onConversationSelected: (ConversationSummary) -> Unit,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
    onToggleMute: (ConversationSummary, Boolean) -> Unit,
    onToggleArchive: (ConversationSummary, Boolean) -> Unit,
    onFilterSelected: (ConversationFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversationStrings = liveChatStrings().conversation
    val uniqueConversations =
        remember(state.conversations, state.searchQuery) {
            state.conversations.distinctBy { it.conversationId }
        }
    val pinned = remember(uniqueConversations, state.searchQuery) { uniqueConversations.filter { it.isPinned } }
    val others = remember(uniqueConversations, state.searchQuery) { uniqueConversations.filterNot { it.isPinned } }
    val selectedFilter = state.selectedFilter

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        if (state.conversations.isNotEmpty() || state.searchQuery.isNotBlank()) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.searchQuery,
                onValueChange = onSearch,
                placeholder = { Text(conversationStrings.searchPlaceholder) },
                singleLine = true,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            items(ConversationFilter.entries.toTypedArray()) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        val label =
                            when (filter) {
                                ConversationFilter.All -> conversationStrings.filterAll
                                ConversationFilter.Unread -> conversationStrings.filterUnread
                                ConversationFilter.Pinned -> conversationStrings.filterPinned
                                ConversationFilter.Archived -> conversationStrings.filterArchived
                            }
                        Text(label)
                    },
                )
            }
        }

        when {
            state.isLoading -> LoadingState(message = conversationStrings.loadingList)
            state.conversations.isEmpty() -> EmptyState(message = conversationStrings.emptyList)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
                ) {
                    if (
                        pinned.isNotEmpty() &&
                        selectedFilter != ConversationFilter.Pinned &&
                        selectedFilter != ConversationFilter.Archived
                    ) {
                        item { SectionHeader(title = conversationStrings.pinnedSectionTitle) }
                        items(pinned, key = { "pinned-${it.conversationId}" }) { summary ->
                            ConversationListRow(
                                summary = summary,
                                onTogglePin = onTogglePin,
                                onToggleMute = onToggleMute,
                                onToggleArchive = onToggleArchive,
                                onClick = onConversationSelected,
                            )
                        }
                        if (others.isNotEmpty()) {
                            item { SectionHeader(title = conversationStrings.othersSectionTitle) }
                        }
                    }
                    items(others, key = { "conv-${it.conversationId}" }) { summary ->
                        ConversationListRow(
                            summary = summary,
                            onTogglePin = onTogglePin,
                            onToggleMute = onToggleMute,
                            onToggleArchive = onToggleArchive,
                            onClick = onConversationSelected,
                        )
                    }
                    if (selectedFilter == ConversationFilter.Pinned) {
                        items(pinned, key = { "pinned-${it.conversationId}" }) { summary ->
                            ConversationListRow(
                                summary = summary,
                                onTogglePin = onTogglePin,
                                onToggleMute = onToggleMute,
                                onToggleArchive = onToggleArchive,
                                onClick = onConversationSelected,
                            )
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun ConversationListScreenPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        ConversationListScreen(
            state = PreviewFixtures.conversationListState(strings),
            onSearch = {},
            onConversationSelected = {},
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onFilterSelected = {},
        )
    }
}
