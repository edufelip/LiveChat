package com.project.livechat.composeapp.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.project.livechat.composeapp.ui.state.collectState
import com.project.livechat.composeapp.ui.state.rememberConversationPresenter
import com.project.livechat.composeapp.ui.state.rememberSessionProvider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationDetailRoute(
    conversationId: String,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        ConversationDetailScreen(
            modifier = modifier,
            state = PreviewFixtures.conversationUiState,
            currentUserId = "preview-user",
            onRefresh = {},
            onSendMessage = {},
            onDismissError = {},
        )
        return
    }

    val presenter = rememberConversationPresenter(conversationId)
    val state by presenter.collectState()
    val sessionProvider = rememberSessionProvider()
    val currentUserId = sessionProvider.currentUserId().orEmpty()

    ConversationDetailScreen(
        modifier = modifier,
        state = state,
        currentUserId = currentUserId,
        onRefresh = { presenter.refresh() },
        onSendMessage = { body -> presenter.sendMessage(body) },
        onDismissError = { presenter.clearError() },
    )
}

@DevicePreviews
@Preview
@Composable
private fun ConversationDetailRoutePreview() {
    LiveChatPreviewContainer {
        ConversationDetailRoute(
            conversationId = PreviewFixtures.conversationUiState.conversationId,
        )
    }
}
