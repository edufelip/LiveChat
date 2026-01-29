package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.platform.openAppSettings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberConversationPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationDetailRoute(
    conversationId: String,
    contactName: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val conversationStrings = strings.conversation
    if (LocalInspectionMode.current) {
        val previewState = PreviewFixtures.conversationUiState(strings)
        ConversationDetailScreen(
            modifier = modifier,
            state = previewState,
            contactName = strings.preview.contactPrimaryName,
            currentUserId = PreviewFixtures.previewUserId(),
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
        return
    }

    val presenter = rememberConversationPresenter(conversationId)
    val state by presenter.collectState()
    val sessionProvider = rememberSessionProvider()
    val currentUserId = sessionProvider.currentUserId().orEmpty()
    val resolvedContactName = state.contactName ?: contactName
    val mediaController = rememberConversationMediaController()
    val scope = rememberCoroutineScope()
    val permissionViewModel = rememberPermissionViewModel()
    val permissionUiState by permissionViewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationMillis by remember { mutableStateOf(0L) }
    var retryCandidate by remember { mutableStateOf<Message?>(null) }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMessageBounds by remember { mutableStateOf<Rect?>(null) }
    var awaitingRetryCompletion by remember { mutableStateOf(false) }
    var scrollToBottomSignal by remember { mutableStateOf(0) }

    val clearSelection: () -> Unit = {
        selectedMessage = null
        selectedMessageBounds = null
    }

    LaunchedEffect(permissionViewModel) {
        permissionViewModel.events.collect { event ->
            when (event) {
                PermissionEvent.OpenSettings -> openAppSettings()
            }
        }
    }

    permissionUiState.dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { permissionViewModel.clearDialog() },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionViewModel.clearDialog()
                        permissionViewModel.requestOpenSettings()
                    },
                ) {
                    Text(strings.general.openSettings)
                }
            },
            dismissButton = {
                TextButton(onClick = { permissionViewModel.clearDialog() }) {
                    Text(strings.general.cancel)
                }
            },
            title = { Text(conversationStrings.permissionTitle) },
            text = { Text(message) },
        )
    }

    retryCandidate?.let { message ->
        AlertDialog(
            onDismissRequest = { retryCandidate = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        retryCandidate = null
                        awaitingRetryCompletion = true
                        presenter.retryMessage(message)
                    },
                ) {
                    Text(conversationStrings.retryMessageCta)
                }
            },
            dismissButton = {
                TextButton(onClick = { retryCandidate = null }) {
                    Text(strings.general.cancel)
                }
            },
            title = { Text(conversationStrings.retryMessageTitle) },
            text = { Text(conversationStrings.retryMessageBody) },
        )
    }

    LaunchedEffect(state.messages, selectedMessage) {
        val selectedId = selectedMessage?.localTempId ?: selectedMessage?.id
        if (selectedId == null) return@LaunchedEffect
        val updatedMessage = state.messages.firstOrNull { it.id == selectedId || it.localTempId == selectedId }
        if (updatedMessage == null) {
            clearSelection()
        } else if (updatedMessage != selectedMessage) {
            selectedMessage = updatedMessage
        }
    }

    LaunchedEffect(state.isSending, state.errorMessage, awaitingRetryCompletion) {
        if (!awaitingRetryCompletion || state.isSending) return@LaunchedEffect
        if (state.errorMessage == null) {
            scrollToBottomSignal += 1
        }
        awaitingRetryCompletion = false
    }

    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        val startedAt = currentEpochMillis()
        recordingDurationMillis = 0L
        while (isRecording) {
            recordingDurationMillis = currentEpochMillis() - startedAt
            delay(250)
        }
    }

    ConversationDetailScreen(
        modifier = modifier,
        state = state,
        contactName = resolvedContactName,
        currentUserId = currentUserId,
        onSendMessage = { body -> presenter.sendMessage(body) },
        isRecording = isRecording,
        recordingDurationMillis = recordingDurationMillis,
        onStartRecording = {
            scope.launch {
                if (isRecording) return@launch
                when (val result = mediaController.startAudioRecording()) {
                    is MediaResult.Success -> {
                        isRecording = true
                        permissionViewModel.clearAll()
                    }
                    is MediaResult.Permission -> {
                        isRecording = false
                        permissionViewModel.handlePermission(
                            status = result.status,
                            hint = conversationStrings.microphonePermissionHint,
                            dialog = conversationStrings.microphonePermissionDialog,
                        )
                    }
                    MediaResult.Cancelled -> {
                        isRecording = false
                        permissionViewModel.clearAll()
                    }
                    is MediaResult.Error -> {
                        isRecording = false
                        permissionViewModel.onError(result.message ?: conversationStrings.recordingStartError)
                    }
                }
            }
        },
        onCancelRecording = {
            scope.launch {
                if (!isRecording) return@launch
                mediaController.stopAudioRecording()
                isRecording = false
                recordingDurationMillis = 0L
            }
        },
        onSendRecording = {
            scope.launch {
                if (!isRecording) return@launch
                val path = mediaController.stopAudioRecording()
                isRecording = false
                recordingDurationMillis = 0L
                path?.let { presenter.sendAudio(it) }
            }
        },
        onPickImage = {
            scope.launch {
                when (val result = mediaController.pickImage()) {
                    is MediaResult.Success -> {
                        presenter.sendImage(result.value)
                        permissionViewModel.clearAll()
                    }
                    is MediaResult.Permission -> {
                        permissionViewModel.handlePermission(
                            status = result.status,
                            hint = conversationStrings.photoPermissionHint,
                            dialog = conversationStrings.photoPermissionDialog,
                        )
                    }
                    MediaResult.Cancelled -> permissionViewModel.clearAll()
                    is MediaResult.Error -> {
                        permissionViewModel.onError(result.message ?: conversationStrings.imageAttachError)
                    }
                }
            }
        },
        onTakePhoto = {
            scope.launch {
                when (val result = mediaController.capturePhoto()) {
                    is MediaResult.Success -> {
                        presenter.sendImage(result.value)
                        permissionViewModel.clearAll()
                    }
                    is MediaResult.Permission -> {
                        permissionViewModel.handlePermission(
                            status = result.status,
                            hint = conversationStrings.cameraPermissionHint,
                            dialog = conversationStrings.cameraPermissionDialog,
                        )
                    }
                    MediaResult.Cancelled -> permissionViewModel.clearAll()
                    is MediaResult.Error -> {
                        permissionViewModel.onError(result.message ?: conversationStrings.photoCaptureError)
                    }
                }
            }
        },
        onBack = {
            if (selectedMessage != null) {
                clearSelection()
            } else {
                onBack()
            }
        },
        onDismissError = { presenter.clearError() },
        onMessageErrorClick = { message ->
            val isOwnMessage =
                message.senderId == currentUserId ||
                    (currentUserId.isBlank() && message.localTempId != null)
            if (isOwnMessage) {
                retryCandidate = message
            }
        },
        snackbarHostState = snackbarHostState,
        selectedMessage = selectedMessage,
        selectedMessageBounds = selectedMessageBounds,
        scrollToBottomSignal = scrollToBottomSignal,
        onMessageLongPress = { message, bounds ->
            selectedMessage = message
            selectedMessageBounds = bounds
        },
        onDismissMessageActions = clearSelection,
        onCopyMessage = { message ->
            clipboardManager.setText(AnnotatedString(message.body))
            scope.launch {
                snackbarHostState.showSnackbar(conversationStrings.messageCopied)
            }
        },
        onDeleteMessage = { message ->
            presenter.deleteMessageLocal(message)
        },
        onRetryMessage = { message ->
            retryCandidate = message
        },
        permissionHint = permissionUiState.hintMessage,
    )
}

@DevicePreviews
@Preview
@Composable
private fun ConversationDetailRoutePreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val previewState = PreviewFixtures.conversationUiState(strings)
        ConversationDetailRoute(
            conversationId = previewState.conversationId,
            contactName = strings.preview.contactPrimaryName,
            onBack = {},
        )
    }
}
