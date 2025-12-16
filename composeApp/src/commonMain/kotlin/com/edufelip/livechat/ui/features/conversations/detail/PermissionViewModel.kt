package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PermissionUiState(
    val hintMessage: String? = null,
    val dialogMessage: String? = null,
)

sealed class PermissionEvent {
    object OpenSettings : PermissionEvent()
}

class PermissionViewModel(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState

    private val eventsChannel = Channel<PermissionEvent>(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: Flow<PermissionEvent> = eventsChannel.receiveAsFlow()

    fun handlePermission(status: PermissionStatus, hint: String, dialog: String) {
        when (status) {
            PermissionStatus.GRANTED -> clearAll()
            PermissionStatus.DENIED -> _uiState.value = PermissionUiState(hintMessage = hint)
            PermissionStatus.BLOCKED -> _uiState.value = PermissionUiState(hintMessage = hint, dialogMessage = dialog)
        }
    }

    fun onError(message: String?) {
        _uiState.value = PermissionUiState(hintMessage = message)
    }

    fun clearDialog() {
        _uiState.update { it.copy(dialogMessage = null) }
    }

    fun clearAll() {
        _uiState.value = PermissionUiState()
    }

    fun requestOpenSettings() {
        scope.launch { eventsChannel.send(PermissionEvent.OpenSettings) }
    }

    fun close() {
        scope.cancel()
    }
}

@Composable
fun rememberPermissionViewModel(): PermissionViewModel {
    val viewModel = remember { PermissionViewModel() }
    DisposableEffect(Unit) {
        onDispose { viewModel.close() }
    }
    return viewModel
}
