package com.edufelip.livechat.ui.features.conversations.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.atoms.Badge
import com.edufelip.livechat.ui.components.molecules.RowWithActions
import com.edufelip.livechat.ui.resources.liveChatStrings
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
    val conversationStrings = liveChatStrings().conversation
    val subtitleContent: (@Composable () -> Unit)? =
        if (summary.lastMessage.contentType == MessageContentType.Audio) {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.mic,
                        contentDescription = conversationStrings.audioMessageLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = conversationStrings.audioShortLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        } else {
            null
        }

    Column(modifier = modifier) {
        RowWithActions(
            title = summary.displayName,
            subtitle = summary.lastMessage.body,
            subtitleContent = subtitleContent,
            endContent = {
                if (summary.unreadCount > 0) {
                    Badge(text = summary.unreadCount.toString())
                    Spacer(modifier = Modifier.height(0.dp))
                }
                IconButton(onClick = { onTogglePin(summary, !summary.isPinned) }) {
                    Icon(
                        imageVector = if (summary.isPinned) AppIcons.pinFilled else AppIcons.pin,
                        contentDescription =
                            if (summary.isPinned) {
                                conversationStrings.unpinAction
                            } else {
                                conversationStrings.pinAction
                            },
                    )
                }
                IconButton(onClick = { onToggleMute(summary, !summary.isMuted) }) {
                    Icon(
                        imageVector = if (summary.isMuted) AppIcons.muted else AppIcons.mute,
                        contentDescription =
                            if (summary.isMuted) {
                                conversationStrings.unmuteAction
                            } else {
                                conversationStrings.muteAction
                            },
                    )
                }
                IconButton(onClick = { onToggleArchive(summary, !summary.isArchived) }) {
                    Icon(
                        imageVector = if (summary.isArchived) AppIcons.unarchive else AppIcons.archive,
                        contentDescription =
                            if (summary.isArchived) {
                                conversationStrings.unarchiveAction
                            } else {
                                conversationStrings.archiveAction
                            },
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
