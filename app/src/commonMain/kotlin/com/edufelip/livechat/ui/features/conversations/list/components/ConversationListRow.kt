package com.edufelip.livechat.ui.features.conversations.list.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.platform.rememberPlatformContext
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import com.edufelip.livechat.ui.util.AvatarImageCache
import com.edufelip.livechat.ui.util.formatAsTime
import com.edufelip.livechat.ui.util.loadAvatarImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.absoluteValue

private val AVATAR_SIZE = 52.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationListRow(
    summary: ConversationSummary,
    currentUserId: String?,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
    onToggleMute: (ConversationSummary, Boolean) -> Unit,
    onToggleArchive: (ConversationSummary, Boolean) -> Unit,
    onClick: (ConversationSummary) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val conversationStrings = liveChatStrings().conversation
    var showMenu by remember { mutableStateOf(false) }
    val onClickAction = rememberStableAction(onClick)
    val onTogglePinAction = rememberStableAction(onTogglePin)
    val onToggleMuteAction = rememberStableAction(onToggleMute)
    val onToggleArchiveAction = rememberStableAction(onToggleArchive)
    val isOutgoing =
        remember(currentUserId, summary.lastMessage.senderId) {
            currentUserId != null && summary.lastMessage.senderId == currentUserId
        }
    val timestamp =
        remember(summary.lastMessage.createdAt) {
            summary.lastMessage.createdAt.formatAsTime()
        }
    val messageLabel =
        remember(summary.lastMessage.contentType, summary.lastMessage.body, conversationStrings) {
            when (summary.lastMessage.contentType) {
                MessageContentType.Audio -> conversationStrings.audioShortLabel
                MessageContentType.Image -> conversationStrings.imageLabel
                else -> summary.lastMessage.body
            }
        }
    val previewText =
        remember(messageLabel, isOutgoing, conversationStrings) {
            if (isOutgoing) {
                AnnotatedString.Builder().apply {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(conversationStrings.youPrefix)
                    }
                    append(" ")
                    append(messageLabel)
                }.toAnnotatedString()
            } else {
                AnnotatedString(messageLabel)
            }
        }

    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onClickAction(summary) },
                        onLongClick = { showMenu = true },
                    ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                ConversationAvatar(
                    displayName = summary.displayName,
                    photoUrl = summary.contactPhoto,
                    isOnline = summary.isOnline,
                    modifier = Modifier.size(AVATAR_SIZE),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = summary.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        ) {
                            Text(
                                text = timestamp,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = AppIcons.chevron,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, fill = false),
                        ) {
                            if (isOutgoing) {
                                MessageStatusIcon(
                                    status = summary.lastMessage.status,
                                )
                            }
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        ) {
                            if (summary.unreadCount > 0) {
                                UnreadBadge(summary.unreadCount)
                            }
                            if (summary.isPinned) {
                                Icon(
                                    imageVector = AppIcons.pinFilled,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp).rotate(45f),
                                )
                            }
                        }
                    }
                }
            }

            ConversationActionsMenu(
                expanded = showMenu,
                summary = summary,
                onDismiss = { showMenu = false },
                onTogglePin = onTogglePinAction,
                onToggleMute = onToggleMuteAction,
                onToggleArchive = onToggleArchiveAction,
            )
        }
        if (showDivider) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            )
        }
    }
}

@Composable
private fun ConversationActionsMenu(
    expanded: Boolean,
    summary: ConversationSummary,
    onDismiss: () -> Unit,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
    onToggleMute: (ConversationSummary, Boolean) -> Unit,
    onToggleArchive: (ConversationSummary, Boolean) -> Unit,
) {
    val conversationStrings = liveChatStrings().conversation
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (summary.isPinned) {
                        conversationStrings.unpinAction
                    } else {
                        conversationStrings.pinAction
                    },
                )
            },
            onClick = {
                onDismiss()
                onTogglePin(summary, !summary.isPinned)
            },
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (summary.isMuted) {
                        conversationStrings.unmuteAction
                    } else {
                        conversationStrings.muteAction
                    },
                )
            },
            onClick = {
                onDismiss()
                onToggleMute(summary, !summary.isMuted)
            },
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (summary.isArchived) {
                        conversationStrings.unarchiveAction
                    } else {
                        conversationStrings.archiveAction
                    },
                )
            },
            onClick = {
                onDismiss()
                onToggleArchive(summary, !summary.isArchived)
            },
        )
    }
}

@Composable
private fun ConversationAvatar(
    displayName: String,
    photoUrl: String?,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    val initials = remember(displayName) { initialsFrom(displayName) }
    val scheme = MaterialTheme.colorScheme
    val colors = remember(displayName, scheme) { avatarGradient(displayName, scheme) }
    val avatarBitmap = rememberAvatarBitmap(photoUrl)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colors)),
        )
        if (initials.isNotBlank()) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap,
                contentDescription = null,
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        if (isOnline) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        MessageStatus.SENDING -> {
            // No icon for sending state to keep it simple
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = AppIcons.check,
                contentDescription = "Sent",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(14.dp),
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = AppIcons.doneAll,
                contentDescription = "Delivered",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(14.dp),
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = AppIcons.doneAll,
                contentDescription = "Read",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.size(14.dp),
            )
        }
        MessageStatus.ERROR -> {
            Icon(
                imageVector = AppIcons.error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun rememberAvatarBitmap(photoUrl: String?): ImageBitmap? {
    val context = rememberPlatformContext()
    val cacheKey = photoUrl?.takeIf { it.isNotBlank() }
    val cachedEntry = cacheKey?.let { AvatarImageCache.getEntry(it) }
    var bitmap by remember(cacheKey) { mutableStateOf(cachedEntry?.bitmap) }

    LaunchedEffect(cacheKey, photoUrl, context) {
        if (photoUrl.isNullOrBlank() || cacheKey == null) return@LaunchedEffect

        // Initial load if not cached
        if (bitmap == null) {
            val loaded = loadAvatarImageBitmap(photoUrl, context)
            if (loaded != null) {
                AvatarImageCache.put(cacheKey, loaded)
                bitmap = loaded
            }
        }

        // Periodic refresh for stale cache entries
        while (isActive) {
            val entry = AvatarImageCache.getEntry(cacheKey)
            val shouldRefresh = entry?.let { AvatarImageCache.isStale(it) } ?: false

            if (shouldRefresh) {
                val loaded = loadAvatarImageBitmap(photoUrl, context)
                if (loaded != null) {
                    AvatarImageCache.put(cacheKey, loaded)
                    bitmap = loaded
                }
            }

            val refreshedEntry = AvatarImageCache.getEntry(cacheKey)
            val delayMs =
                refreshedEntry?.let { AvatarImageCache.timeUntilStale(it) }
                    ?: AvatarImageCache.MIN_REFRESH_INTERVAL_MS
            delay(delayMs.coerceAtLeast(AvatarImageCache.MIN_REFRESH_INTERVAL_MS))
        }
    }
    return bitmap
}

private fun initialsFrom(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts.first().take(2).uppercase()
    return (parts.first().take(1) + parts.last().take(1)).uppercase()
}

private fun avatarGradient(
    name: String,
    scheme: ColorScheme,
): List<Color> {
    val options =
        listOf(
            scheme.primary to scheme.tertiary,
            scheme.secondary to scheme.primary,
            scheme.tertiary to scheme.secondary,
        )
    val (start, end) = options[name.hashCode().absoluteValue % options.size]
    return listOf(start, end)
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun <T1, T2> rememberStableAction(action: (T1, T2) -> Unit): (T1, T2) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value1, value2 -> actionState.value(value1, value2) } }
}

@DevicePreviews
@Preview
@Composable
private fun ConversationListRowPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val state = PreviewFixtures.conversationListState(strings)
        ConversationListRow(
            summary = state.conversations.first(),
            currentUserId = state.currentUserId,
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onClick = {},
        )
    }
}
