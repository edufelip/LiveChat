package com.edufelip.livechat.ui.features.conversations.detail.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.molecules.ErrorBanner
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.features.conversations.detail.rememberAudioPlayerController
import com.edufelip.livechat.ui.features.conversations.detail.components.ComposerBar
import com.edufelip.livechat.ui.features.conversations.detail.components.MessageBubble
import com.edufelip.livechat.ui.features.conversations.detail.components.rememberLazyListStateWithAutoscroll
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.util.formatAsTime
import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.Message
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConversationDetailScreen(
    state: ConversationUiState,
    contactName: String?,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    isRecording: Boolean,
    recordingDurationMillis: Long,
    onStartRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSendRecording: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit,
    permissionHint: String?,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val resolvedTitle =
        remember(contactName, state.contactName) {
            contactName?.takeIf { it.isNotBlank() }
                ?: state.contactName?.takeIf { it.isNotBlank() }
                ?: strings.home.conversationTitle
        }
    val conversationStrings = strings.conversation
    val audioController = rememberAudioPlayerController()
    val playingPath by audioController.playingPath.collectAsState()
    val isPlaying by audioController.isPlaying.collectAsState()
    val progress by audioController.progress.collectAsState()
    val duration by audioController.durationMillis.collectAsState()
    val position by audioController.positionMillis.collectAsState()
    val scope = rememberCoroutineScope()
    val onAudioToggle: (String) -> Unit =
        remember(playingPath, isPlaying) {
            { path ->
                scope.launch {
                    if (playingPath == path && isPlaying) {
                        audioController.stop()
                    } else {
                        audioController.play(path)
                    }
                }
            }
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = resolvedTitle,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = AppIcons.back, contentDescription = strings.home.backCta)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isArchived) {
                Text(
                    text = conversationStrings.archivedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.isMuted) {
                val muteLabel =
                    state.muteUntil?.let { conversationStrings.mutedUntilPrefix + " " + it.formatAsTime() }
                        ?: conversationStrings.mutedLabel
                Text(
                    text = muteLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when {
                state.isLoading && state.messages.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingState(message = conversationStrings.loadingMessages)
                    }
                }
                else -> {
                    ConversationMessagesList(
                        modifier = Modifier.weight(1f),
                        messages = state.messages,
                        currentUserId = currentUserId,
                        playingPath = playingPath,
                        isPlaying = isPlaying,
                        progress = progress,
                        duration = duration,
                        position = position,
                        onAudioToggle = onAudioToggle,
                    )
                }
            }

            state.errorMessage?.let { message ->
                ErrorBanner(message = message, onDismiss = onDismissError)
            }

            RecordingControlsBar(
                isRecording = isRecording,
                durationMillis = recordingDurationMillis,
                onCancel = onCancelRecording,
                onSend = onSendRecording,
            )
            PermissionHint(hint = permissionHint)

            ComposerBar(
                isSending = state.isSending,
                errorMessage = state.errorMessage,
                onSend = onSendMessage,
                isRecording = isRecording,
                onStartRecording = onStartRecording,
                onPickImage = onPickImage,
                onTakePhoto = onTakePhoto,
                onErrorClick = onDismissError,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConversationMessagesList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    currentUserId: String,
    playingPath: String?,
    isPlaying: Boolean,
    progress: Float,
    duration: Long,
    position: Long,
    onAudioToggle: (String) -> Unit,
) {
    val listState = rememberLazyListStateWithAutoscroll(messages)
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(
            items = messages,
            key = { message -> "${message.id}:${message.localTempId ?: message.createdAt}" },
        ) { message ->
            MessageBubble(
                message = message,
                isOwn = message.senderId == currentUserId,
                isPlaying = message.body == playingPath && isPlaying,
                onAudioToggle = onAudioToggle,
                progress = if (playingPath == message.body) progress else 0f,
                durationMillis = if (playingPath == message.body) duration else 0L,
                positionMillis = if (playingPath == message.body) position else 0L,
            )
        }
    }
}

@Composable
private fun RecordingControlsBar(
    isRecording: Boolean,
    durationMillis: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isRecording,
        enter = fadeIn() + slideInVertically { fullHeight -> -fullHeight / 2 },
        exit = fadeOut() + slideOutVertically { fullHeight -> -fullHeight / 2 },
        modifier = modifier.fillMaxWidth(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel recording",
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Recording ${formatDurationMillis(durationMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                FilledIconButton(onClick = onSend) {
                    Icon(imageVector = AppIcons.confirm, contentDescription = "Send recording")
                }
            }
        }
    }
}

@Composable
private fun PermissionHint(hint: String?) {
    hint ?: return
    Text(
        text = hint,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

@DevicePreviews
@Preview
@Composable
private fun ConversationDetailScreenPreview() {
    LiveChatPreviewContainer {
        ConversationDetailScreen(
            state = PreviewFixtures.conversationUiState,
            contactName = "Ava Harper",
            currentUserId = "preview-user",
            onSendMessage = {},
            isRecording = false,
            recordingDurationMillis = 0L,
            onStartRecording = {},
            onCancelRecording = {},
            onSendRecording = {},
            onPickImage = {},
            onTakePhoto = {},
            onBack = {},
            onDismissError = {},
            permissionHint = null,
        )
    }
}

private fun formatDurationMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val minutesText = minutes.toString().padStart(2, '0')
    val secondsText = seconds.toString().padStart(2, '0')
    return "$minutesText:$secondsText"
}

@DevicePreviews
@Preview
@Composable
private fun ConversationMessagesListPreview() {
    LiveChatPreviewContainer {
        ConversationMessagesList(
            messages = PreviewFixtures.conversationUiState.messages,
            currentUserId = "preview-user",
            playingPath = null,
            isPlaying = false,
            progress = 0f,
            duration = 0L,
            position = 0L,
            onAudioToggle = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun RecordingIndicatorPreview() {
    LiveChatPreviewContainer {
        RecordingControlsBar(
            isRecording = true,
            durationMillis = 12_000,
            onCancel = {},
            onSend = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun PermissionHintPreview() {
    LiveChatPreviewContainer {
        PermissionHint(hint = "Camera permission needed")
    }
}
