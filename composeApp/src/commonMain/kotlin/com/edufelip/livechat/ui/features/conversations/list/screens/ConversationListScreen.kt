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
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.components.atoms.SectionHeader
import com.edufelip.livechat.ui.components.molecules.EmptyState
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.features.conversations.list.components.ConversationListRow
import com.edufelip.livechat.ui.theme.spacing
import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationSummary
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
    val pinned =
        remember(state.conversations, state.searchQuery) {
            state.conversations.filter { it.isPinned }
        }
    val others =
        remember(state.conversations, state.searchQuery) {
            state.conversations.filterNot { it.isPinned }
        }
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
                placeholder = { Text("Search conversations") },
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
                    label = { Text(filter.displayName) },
                )
            }
        }

        when {
            state.isLoading -> LoadingState(message = "Loading conversations…")
            state.conversations.isEmpty() -> EmptyState(message = "No conversations yet")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
                ) {
                    if (pinned.isNotEmpty() && selectedFilter != ConversationFilter.Pinned && selectedFilter != ConversationFilter.Archived) {
                        item { SectionHeader(title = "Pinned") }
                        items(pinned, key = { it.conversationId }) { summary ->
                            ConversationListRow(
                                summary = summary,
                                onTogglePin = onTogglePin,
                                onToggleMute = onToggleMute,
                                onToggleArchive = onToggleArchive,
                                onClick = onConversationSelected,
                            )
                        }
                        if (others.isNotEmpty()) {
                            item { SectionHeader(title = "Others") }
                        }
                    }
                    items(others, key = { it.conversationId }) { summary ->
                        ConversationListRow(
                            summary = summary,
                            onTogglePin = onTogglePin,
                            onToggleMute = onToggleMute,
                            onToggleArchive = onToggleArchive,
                            onClick = onConversationSelected,
                        )
                    }
                    if (selectedFilter == ConversationFilter.Pinned) {
                        items(pinned, key = { it.conversationId }) { summary ->
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
        ConversationListScreen(
            state = PreviewFixtures.conversationListState,
            onSearch = {},
            onConversationSelected = {},
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onFilterSelected = {},
        )
    }
}
