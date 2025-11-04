package com.project.livechat.composeapp.ui.features.conversations.list.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.components.atoms.SectionHeader
import com.project.livechat.composeapp.ui.components.molecules.EmptyState
import com.project.livechat.composeapp.ui.components.molecules.LoadingState
import com.project.livechat.composeapp.ui.features.conversations.list.components.ConversationListRow
import com.project.livechat.domain.models.ConversationListUiState
import com.project.livechat.domain.models.ConversationSummary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationListScreen(
    state: ConversationListUiState,
    onSearch: (String) -> Unit,
    onConversationSelected: (ConversationSummary) -> Unit,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = onSearch,
            placeholder = { Text("Search conversations") },
            singleLine = true,
            colors = TextFieldDefaults.colors(),
        )

        when {
            state.isLoading -> LoadingState(message = "Loading conversations…")
            state.conversations.isEmpty() -> EmptyState(message = "No conversations yet")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    if (pinned.isNotEmpty()) {
                        item { SectionHeader(title = "Pinned") }
                        items(pinned, key = { it.conversationId }) { summary ->
                            ConversationListRow(
                                summary = summary,
                                onTogglePin = onTogglePin,
                                onClick = onConversationSelected,
                            )
                        }
                        item { SectionHeader(title = "Others") }
                    }
                    items(others, key = { it.conversationId }) { summary ->
                        ConversationListRow(
                            summary = summary,
                            onTogglePin = onTogglePin,
                            onClick = onConversationSelected,
                        )
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
        )
    }
}
