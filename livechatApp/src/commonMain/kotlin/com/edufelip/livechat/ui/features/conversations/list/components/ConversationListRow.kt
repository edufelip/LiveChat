package com.edufelip.livechat.ui.features.conversations.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.atoms.Badge
import com.edufelip.livechat.ui.components.molecules.RowWithActions
import com.edufelip.livechat.domain.models.ConversationSummary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationListRow(
    summary: ConversationSummary,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
    onToggleMute: (ConversationSummary, Boolean) -> Unit,
    onToggleArchive: (ConversationSummary, Boolean) -> Unit,
    onClick: (ConversationSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        RowWithActions(
            title = summary.displayName,
            subtitle = summary.lastMessage.body,
            endContent = {
                if (summary.unreadCount > 0) {
                    Badge(text = summary.unreadCount.toString())
                    Spacer(modifier = Modifier.height(0.dp))
                }
                IconButton(onClick = { onTogglePin(summary, !summary.isPinned) }) {
                    Icon(
                        imageVector = if (summary.isPinned) AppIcons.pinFilled else AppIcons.pin,
                        contentDescription = if (summary.isPinned) "Unpin" else "Pin",
                    )
                }
                IconButton(onClick = { onToggleMute(summary, !summary.isMuted) }) {
                    Icon(
                        imageVector = if (summary.isMuted) AppIcons.muted else AppIcons.mute,
                        contentDescription = if (summary.isMuted) "Unmute" else "Mute",
                    )
                }
                IconButton(onClick = { onToggleArchive(summary, !summary.isArchived) }) {
                    Icon(
                        imageVector = if (summary.isArchived) AppIcons.unarchive else AppIcons.archive,
                        contentDescription = if (summary.isArchived) "Unarchive" else "Archive",
                    )
                }
            },
            onClick = { onClick(summary) },
            highlight = summary.isPinned,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun ConversationListRowPreview() {
    LiveChatPreviewContainer {
        ConversationListRow(
            summary = PreviewFixtures.conversationListState.conversations.first(),
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onClick = {},
        )
    }
}
