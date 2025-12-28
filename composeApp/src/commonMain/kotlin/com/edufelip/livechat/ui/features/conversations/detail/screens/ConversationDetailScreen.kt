package com.edufelip.livechat.ui.features.conversations.detail.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.molecules.ErrorBanner
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.features.conversations.detail.components.ComposerBar
import com.edufelip.livechat.ui.features.conversations.detail.components.MessageBubble
import com.edufelip.livechat.ui.features.conversations.detail.components.rememberLazyListStateWithAutoscroll
import com.edufelip.livechat.ui.features.conversations.detail.rememberAudioPlayerController
import com.edufelip.livechat.ui.platform.isAndroid
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.util.formatAsTime
import com.edufelip.livechat.ui.util.formatDurationMillis
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

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
    onMessageErrorClick: (Message) -> Unit,
    snackbarHostState: SnackbarHostState,
    selectedMessage: Message?,
    selectedMessageBounds: Rect?,
    scrollToBottomSignal: Int,
    onMessageLongPress: (Message, Rect) -> Unit,
    onDismissMessageActions: () -> Unit,
    onCopyMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onRetryMessage: (Message) -> Unit,
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
    val onBackState by rememberUpdatedState(onBack)
    val density = LocalDensity.current
    val edgeWidthPx = remember(density) { with(density) { 24.dp.toPx() } }
    val swipeThresholdPx = remember(density) { with(density) { 72.dp.toPx() } }
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
    val swipeBackModifier =
        if (isAndroid()) {
            Modifier
        } else {
            Modifier.pointerInput(edgeWidthPx, swipeThresholdPx, onBackState) {
                var fromEdge = false
                var dragDistance = 0f
                var triggered = false
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        fromEdge = offset.x <= edgeWidthPx
                        dragDistance = 0f
                        triggered = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (!fromEdge || triggered) return@detectHorizontalDragGestures
                        if (dragAmount > 0f) {
                            dragDistance += dragAmount
                        } else {
                            dragDistance = (dragDistance + dragAmount).coerceAtLeast(0f)
                        }
                        change.consume()
                        if (dragDistance >= swipeThresholdPx) {
                            triggered = true
                            onBackState()
                        }
                    },
                    onDragEnd = {
                        fromEdge = false
                        dragDistance = 0f
                        triggered = false
                    },
                    onDragCancel = {
                        fromEdge = false
                        dragDistance = 0f
                        triggered = false
                    },
                )
            }
        }

    val activeMessage = selectedMessage
    val activeBounds = selectedMessageBounds
    val selectionActive = activeMessage != null && activeBounds != null
    val contentBlurModifier = if (selectionActive && isAndroid()) Modifier.blur(8.dp) else Modifier
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var rootOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .then(swipeBackModifier)
                .onGloballyPositioned {
                    rootSize = it.size
                    rootOffset = it.positionInWindow()
                },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().then(contentBlurModifier),
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            selectedMessageId = activeMessage?.localTempId ?: activeMessage?.id,
                            onAudioToggle = onAudioToggle,
                            onMessageLongPress = onMessageLongPress,
                            onMessageErrorClick = onMessageErrorClick,
                            rootOffset = rootOffset,
                            scrollToBottomSignal = scrollToBottomSignal,
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

        AnimatedVisibility(
            visible = selectionActive,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (activeMessage != null && activeBounds != null) {
                MessageActionsOverlay(
                    message = activeMessage,
                    messageBounds = activeBounds,
                    rootSize = rootSize,
                    currentUserId = currentUserId,
                    onDismiss = onDismissMessageActions,
                    onCopy = {
                        onCopyMessage(activeMessage)
                        onDismissMessageActions()
                    },
                    onDelete = {
                        onDeleteMessage(activeMessage)
                        onDismissMessageActions()
                    },
                    onRetry = {
                        onRetryMessage(activeMessage)
                        onDismissMessageActions()
                    },
                )
            }
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
    selectedMessageId: String?,
    onAudioToggle: (String) -> Unit,
    onMessageLongPress: (Message, Rect) -> Unit,
    onMessageErrorClick: (Message) -> Unit,
    rootOffset: Offset,
    scrollToBottomSignal: Int,
) {
    val listState = rememberLazyListStateWithAutoscroll(messages)
    LaunchedEffect(scrollToBottomSignal) {
        if (scrollToBottomSignal > 0 && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
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
            val isOwnMessage =
                message.senderId == currentUserId ||
                    (currentUserId.isBlank() && message.localTempId != null)
            val messageKey = message.localTempId ?: message.id
            val isHighlighted = selectedMessageId != null && selectedMessageId == messageKey
            var bubbleBounds by remember(messageKey) { mutableStateOf<Rect?>(null) }
            MessageBubble(
                message = message,
                isOwn = isOwnMessage,
                isPlaying = message.body == playingPath && isPlaying,
                onAudioToggle = onAudioToggle,
                progress = if (playingPath == message.body) progress else 0f,
                durationMillis = if (playingPath == message.body) duration else 0L,
                positionMillis = if (playingPath == message.body) position else 0L,
                onErrorClick = { onMessageErrorClick(message) },
                onLongPress = { bubbleBounds?.let { bounds -> onMessageLongPress(message, bounds) } },
                onBubblePositioned = { bounds -> bubbleBounds = bounds.toRootBounds(rootOffset) },
                highlighted = isHighlighted,
            )
        }
    }
}

@Composable
private fun MessageActionsOverlay(
    message: Message,
    messageBounds: Rect,
    rootSize: IntSize,
    currentUserId: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
) {
    val conversationStrings = liveChatStrings().conversation
    val density = LocalDensity.current
    val cutoutRadius = with(density) { 16.dp.toPx() }
    val gapPx = with(density) { 8.dp.toPx() }
    val edgePaddingPx = with(density) { 12.dp.toPx() }
    val dimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
    val isOwnMessage =
        message.senderId == currentUserId ||
            (currentUserId.isBlank() && message.localTempId != null)
    val copyEnabled =
        message.contentType != MessageContentType.Image &&
            message.contentType != MessageContentType.Audio
    val showRetry = message.status == MessageStatus.ERROR && isOwnMessage
    val safeBounds = remember(messageBounds, rootSize) { clampBounds(messageBounds, rootSize) }
    var actionBoxSize by remember(message.id, message.localTempId) { mutableStateOf(IntSize.Zero) }
    val actionOffset =
        remember(actionBoxSize, safeBounds, rootSize, isOwnMessage) {
            calculateActionOffset(
                messageBounds = safeBounds,
                actionBoxSize = actionBoxSize,
                rootSize = rootSize,
                gapPx = gapPx,
                edgePaddingPx = edgePaddingPx,
                alignToEnd = isOwnMessage,
            )
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
                    .drawWithContent {
                        drawRect(dimColor)
                        val cutoutSize = Size(safeBounds.width, safeBounds.height)
                        if (cutoutSize.width > 0f && cutoutSize.height > 0f) {
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = Offset(safeBounds.left, safeBounds.top),
                                size = cutoutSize,
                                cornerRadius = CornerRadius(cutoutRadius, cutoutRadius),
                                blendMode = BlendMode.Clear,
                            )
                        }
                    },
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            modifier =
                Modifier
                    .offset { actionOffset }
                    .onGloballyPositioned { coordinates -> actionBoxSize = coordinates.size }
                    .zIndex(1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MessageActionButton(
                    label = conversationStrings.copyAction,
                    icon = Icons.Rounded.ContentCopy,
                    enabled = copyEnabled,
                    onClick = onCopy,
                )
                MessageActionButton(
                    label = conversationStrings.deleteAction,
                    icon = Icons.Rounded.Delete,
                    enabled = true,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onDelete,
                )
                if (showRetry) {
                    MessageActionButton(
                        label = conversationStrings.retryMessageCta,
                        icon = Icons.Rounded.Refresh,
                        enabled = true,
                        onClick = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val contentColor =
        if (enabled) {
            tint
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        }
    Column(
        modifier =
            modifier
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

private fun clampBounds(
    bounds: Rect,
    rootSize: IntSize,
): Rect {
    val maxWidth = rootSize.width.takeIf { it > 0 }?.toFloat() ?: bounds.right
    val maxHeight = rootSize.height.takeIf { it > 0 }?.toFloat() ?: bounds.bottom
    val left = bounds.left.coerceIn(0f, maxWidth)
    val top = bounds.top.coerceIn(0f, maxHeight)
    val right = bounds.right.coerceIn(left, maxWidth)
    val bottom = bounds.bottom.coerceIn(top, maxHeight)
    return Rect(left, top, right, bottom)
}

private fun Rect.toRootBounds(rootOffset: Offset): Rect =
    Rect(
        left = left - rootOffset.x,
        top = top - rootOffset.y,
        right = right - rootOffset.x,
        bottom = bottom - rootOffset.y,
    )

private fun calculateActionOffset(
    messageBounds: Rect,
    actionBoxSize: IntSize,
    rootSize: IntSize,
    gapPx: Float,
    edgePaddingPx: Float,
    alignToEnd: Boolean,
): IntOffset {
    val actionWidth = actionBoxSize.width
    val actionHeight = actionBoxSize.height
    val baseX =
        if (alignToEnd && actionWidth > 0) {
            messageBounds.right - actionWidth
        } else {
            messageBounds.left
        }
    var x = baseX
    if (rootSize.width > 0 && actionWidth > 0) {
        val minX = edgePaddingPx
        val maxX = (rootSize.width - actionWidth - edgePaddingPx).coerceAtLeast(minX)
        x = x.coerceIn(minX, maxX)
    }

    var y = messageBounds.bottom + gapPx
    if (rootSize.height > 0 && actionHeight > 0 && y + actionHeight + edgePaddingPx > rootSize.height) {
        y = messageBounds.top - actionHeight - gapPx
    }
    if (rootSize.height > 0 && actionHeight > 0) {
        val minY = edgePaddingPx
        val maxY = (rootSize.height - actionHeight - edgePaddingPx).coerceAtLeast(minY)
        y = y.coerceIn(minY, maxY)
    }
    return IntOffset(x.roundToInt(), y.roundToInt())
}

@Composable
private fun RecordingControlsBar(
    isRecording: Boolean,
    durationMillis: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversationStrings = liveChatStrings().conversation
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
                        contentDescription = conversationStrings.recordingCancelDescription,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = conversationStrings.recordingLabel(formatDurationMillis(durationMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                FilledIconButton(onClick = onSend) {
                    Icon(
                        imageVector = AppIcons.confirm,
                        contentDescription = conversationStrings.recordingSendDescription,
                    )
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
            onMessageErrorClick = {},
            snackbarHostState = remember { SnackbarHostState() },
            selectedMessage = null,
            selectedMessageBounds = null,
            scrollToBottomSignal = 0,
            onMessageLongPress = { _, _ -> },
            onDismissMessageActions = {},
            onCopyMessage = {},
            onDeleteMessage = {},
            onRetryMessage = {},
            permissionHint = null,
        )
    }
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
            selectedMessageId = null,
            onAudioToggle = {},
            onMessageLongPress = { _, _ -> },
            onMessageErrorClick = {},
            rootOffset = Offset.Zero,
            scrollToBottomSignal = 0,
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
