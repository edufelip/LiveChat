package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberConversationPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationDetailRoute(
    conversationId: String,
    contactName: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        ConversationDetailScreen(
            modifier = modifier,
            state = PreviewFixtures.conversationUiState,
            contactName = "Preview Contact",
            currentUserId = "preview-user",
            onSendMessage = {},
            onBack = {},
            onDismissError = {},
        )
        return
    }

    val presenter = rememberConversationPresenter(conversationId)
    val state by presenter.collectState()
    val sessionProvider = rememberSessionProvider()
    val currentUserId = sessionProvider.currentUserId().orEmpty()
    val resolvedContactName = state.contactName ?: contactName

    ConversationDetailScreen(
        modifier = modifier,
        state = state,
        contactName = resolvedContactName,
        currentUserId = currentUserId,
        onSendMessage = { body -> presenter.sendMessage(body) },
        onBack = onBack,
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
            contactName = "Preview Contact",
            onBack = {},
        )
    }
}
