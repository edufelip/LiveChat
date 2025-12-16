package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.features.conversations.detail.rememberConversationMediaController
import com.edufelip.livechat.ui.features.conversations.detail.rememberPermissionViewModel
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberConversationPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import kotlinx.coroutines.launch
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
            isRecording = false,
            onToggleRecording = {},
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
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { permissionViewModel.clearDialog() }) {
                    Text("Cancel")
                }
            },
            title = { Text("Permission needed") },
            text = { Text(message) },
        )
    }

    ConversationDetailScreen(
        modifier = modifier,
        state = state,
        contactName = resolvedContactName,
        currentUserId = currentUserId,
        onSendMessage = { body -> presenter.sendMessage(body) },
        isRecording = isRecording,
        onToggleRecording = {
            scope.launch {
                if (isRecording) {
                    val path = mediaController.stopAudioRecording()
                    isRecording = false
                    path?.let { presenter.sendAudio(it) }
                } else {
                    when (val result = mediaController.startAudioRecording()) {
                        is MediaResult.Success -> {
                            isRecording = true
                            permissionViewModel.clearAll()
                        }
                        is MediaResult.Permission -> {
                            isRecording = false
                            permissionViewModel.handlePermission(
                                status = result.status,
                                hint = "Microphone permission is required to record audio.",
                                dialog = "Microphone permission is blocked. Please enable it in Settings.",
                            )
                        }
                        MediaResult.Cancelled -> {
                            isRecording = false
                            permissionViewModel.clearAll()
                        }
                        is MediaResult.Error -> {
                            isRecording = false
                            permissionViewModel.onError(result.message ?: "Unable to start recording.")
                        }
                    }
                }
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
                            hint = "Allow photo/gallery access to attach images.",
                            dialog = "Photo permissions are blocked. Please enable them in Settings.",
                        )
                    }
                    MediaResult.Cancelled -> permissionViewModel.clearAll()
                    is MediaResult.Error -> {
                        permissionViewModel.onError(result.message ?: "Unable to attach image.")
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
                            hint = "Camera permission is required to take a photo.",
                            dialog = "Camera permission is blocked. Please enable it in Settings.",
                        )
                    }
                    MediaResult.Cancelled -> permissionViewModel.clearAll()
                    is MediaResult.Error -> {
                        permissionViewModel.onError(result.message ?: "Unable to capture photo.")
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
