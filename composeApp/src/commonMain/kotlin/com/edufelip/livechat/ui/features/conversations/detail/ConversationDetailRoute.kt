package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
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
        ConversationDetailScreen(
            modifier = modifier,
            state = PreviewFixtures.conversationUiState,
            contactName = "Preview Contact",
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
    var isRecording by androidx.compose.runtime.remember { mutableStateOf(false) }
    var recordingDurationMillis by androidx.compose.runtime.remember { mutableStateOf(0L) }

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
        onBack = onBack,
        onDismissError = { presenter.clearError() },
        permissionHint = permissionUiState.hintMessage,
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
