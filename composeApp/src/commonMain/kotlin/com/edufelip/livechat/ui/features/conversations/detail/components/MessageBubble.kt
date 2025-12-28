package com.edufelip.livechat.ui.features.conversations.detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.loadLocalImageBitmap
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.util.formatAsTime
import com.edufelip.livechat.ui.util.formatDurationMillis
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Single chat bubble aligned by sender ownership.
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    isPlaying: Boolean,
    onAudioToggle: (String) -> Unit,
    progress: Float,
    durationMillis: Long,
    positionMillis: Long,
    modifier: Modifier = Modifier,
    onErrorClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onBubblePositioned: ((Rect) -> Unit)? = null,
    highlighted: Boolean = false,
) {
    val conversationStrings = liveChatStrings().conversation
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bubbleColor =
        if (isOwn) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    val textColor =
        if (isOwn) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val bubbleShape = RoundedCornerShape(16.dp)
    val highlightBorderColor =
        if (isOwn) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primary
        }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxBubbleWidth = maxWidth * 0.8f
        val showError = isOwn && message.status == MessageStatus.ERROR && onErrorClick != null
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = alignment,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp,
                        if (isOwn) Alignment.End else Alignment.Start,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showError) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = conversationStrings.messageFailed,
                        tint = MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier
                                .size(18.dp)
                                .clickable(onClick = onErrorClick)
                                .semantics { role = Role.Button },
                    )
                }
                val bubbleModifier =
                    Modifier
                        .wrapContentWidth()
                        .widthIn(max = maxBubbleWidth)
                        .then(
                            if (onLongPress != null) {
                                Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = onLongPress,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .then(
                            if (onBubblePositioned != null) {
                                Modifier.onGloballyPositioned { coordinates ->
                                    onBubblePositioned(coordinates.boundsInRoot())
                                }
                            } else {
                                Modifier
                            },
                        )
                        .then(
                            if (highlighted) {
                                Modifier.border(1.dp, highlightBorderColor, bubbleShape)
                            } else {
                                Modifier
                            },
                        )

                Surface(
                    color = bubbleColor,
                    shape = bubbleShape,
                    modifier = bubbleModifier,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        when (message.contentType) {
                            MessageContentType.Image ->
                                ImageBubbleContent(
                                    message = message,
                                    description = conversationStrings.imageMessageDescription,
                                    label = conversationStrings.imageLabel,
                                    fallbackTemplate = conversationStrings.imageFallbackLabel,
                                )
                            MessageContentType.Audio ->
                                AudioBubbleContent(
                                    message = message,
                                    textColor = textColor,
                                    isPlaying = isPlaying,
                                    onAudioToggle = onAudioToggle,
                                    progress = progress,
                                    durationMillis = durationMillis,
                                    positionMillis = positionMillis,
                                    playDescription = conversationStrings.playAudioDescription,
                                    stopDescription = conversationStrings.stopAudioDescription,
                                    playingLabel = conversationStrings.playingAudioLabel,
                                    idleLabel = conversationStrings.audioMessageLabel,
                                )
                            else ->
                                Text(
                                    text = message.body,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = message.createdAt.formatAsTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.8f),
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
private fun MessageBubblePreview() {
    LiveChatPreviewContainer {
        val message = PreviewFixtures.conversationUiState.messages.first()
        MessageBubble(
            message = message,
            isOwn = message.senderId == "user",
            isPlaying = false,
            onAudioToggle = {},
            progress = 0f,
            durationMillis = 0,
            positionMillis = 0,
        )
    }
}

@Composable
private fun AudioBubbleContent(
    message: Message,
    textColor: androidx.compose.ui.graphics.Color,
    isPlaying: Boolean,
    onAudioToggle: (String) -> Unit,
    progress: Float,
    durationMillis: Long,
    positionMillis: Long,
    playDescription: String,
    stopDescription: String,
    playingLabel: String,
    idleLabel: String,
    modifier: Modifier = Modifier,
) {
    val safeDuration = durationMillis.coerceAtLeast(0L)
    val maxPosition = if (safeDuration > 0) safeDuration else Long.MAX_VALUE
    val safePosition = positionMillis.coerceIn(0L, maxPosition)
    val displayProgress = progress.coerceIn(0f, 1f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.wrapContentWidth(),
    ) {
        val icon = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow
        IconButton(onClick = { onAudioToggle(message.body) }) {
            Icon(
                imageVector = icon,
                contentDescription = if (isPlaying) stopDescription else playDescription,
                tint = textColor,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isPlaying) playingLabel else idleLabel,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = displayProgress,
                modifier = Modifier.fillMaxWidth(),
                color = textColor,
                trackColor = textColor.copy(alpha = 0.25f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDurationMillis(safePosition),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = formatDurationMillis(safeDuration),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ImageBubbleContent(
    message: Message,
    description: String,
    label: String,
    fallbackTemplate: @Composable (String) -> String,
) {
    val bitmap = remember(message.body) { loadLocalImageBitmap(message.body) }
    if (bitmap != null) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            bitmap = bitmap,
            contentDescription = description,
            contentScale = ContentScale.Crop,
        )
    } else {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = Icons.Rounded.Image, contentDescription = label)
            Text(
                text = fallbackTemplate(message.body.substringAfterLast('/')),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
