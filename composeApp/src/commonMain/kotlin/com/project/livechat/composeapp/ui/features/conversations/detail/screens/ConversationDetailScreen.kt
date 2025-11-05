package com.project.livechat.composeapp.ui.features.conversations.detail.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.components.molecules.ErrorBanner
import com.project.livechat.composeapp.ui.components.molecules.LoadingState
import com.project.livechat.composeapp.ui.features.conversations.detail.components.ComposerBar
import com.project.livechat.composeapp.ui.features.conversations.detail.components.MessageBubble
import com.project.livechat.composeapp.ui.features.conversations.detail.components.rememberLazyListStateWithAutoscroll
import com.project.livechat.domain.models.ConversationUiState
import com.project.livechat.domain.models.Message
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationDetailScreen(
    state: ConversationUiState,
    currentUserId: String,
    onRefresh: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            state.isLoading && state.messages.isEmpty() -> {
                LoadingState(message = "Loading messages…")
            }
            else -> {
                val listState = rememberLazyListStateWithAutoscroll(state.messages)
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    items(state.messages, key = Message::id) { message ->
                        MessageBubble(
                            message = message,
                            isOwn = message.senderId == currentUserId,
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { message ->
            ErrorBanner(message = message, onDismiss = onDismissError)
        }

        ComposerBar(
            isSending = state.isSending,
            onSend = onSendMessage,
            onRefresh = onRefresh,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun ConversationDetailScreenPreview() {
    LiveChatPreviewContainer {
        ConversationDetailScreen(
            state = PreviewFixtures.conversationUiState,
            currentUserId = "preview-user",
            onRefresh = {},
            onSendMessage = {},
            onDismissError = {},
        )
    }
}
