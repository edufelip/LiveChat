package com.edufelip.livechat.ui.features.conversations.list

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.list.screens.ConversationListScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberConversationListPresenter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationListRoute(
    onConversationSelected: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        val previewState = remember { PreviewFixtures.conversationListState }
        ConversationListScreen(
            modifier = modifier,
            state = previewState,
            onSearch = {},
            onConversationSelected = {},
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onFilterSelected = {},
        )
        return
    }

    val presenter = rememberConversationListPresenter()
    val state by presenter.collectState()

    var showErrorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    val errorMessage = state.errorMessage
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                presenter.clearError()
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    presenter.clearError()
                }) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(strings.general.errorTitle) },
            text = { Text(errorMessage) },
        )
    }

    ConversationListScreen(
        modifier = modifier,
        state = state,
        onSearch = { presenter.setSearchQuery(it) },
        onConversationSelected = { summary ->
            onConversationSelected(summary.conversationId, summary.contactName)
        },
        onTogglePin = { summary, pinned ->
            presenter.togglePinned(summary.conversationId, pinned)
        },
        onToggleMute = { summary, muted ->
            presenter.toggleMuted(summary.conversationId, muted)
        },
        onToggleArchive = { summary, archived ->
            presenter.toggleArchived(summary.conversationId, archived)
        },
        onFilterSelected = { presenter.setFilter(it) },
    )
}

@DevicePreviews
@Preview
@Composable
private fun ConversationListRoutePreview() {
    LiveChatPreviewContainer {
        ConversationListRoute(onConversationSelected = { _, _ -> })
    }
}
