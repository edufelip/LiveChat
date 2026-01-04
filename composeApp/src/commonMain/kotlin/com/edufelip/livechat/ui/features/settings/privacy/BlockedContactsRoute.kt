package com.edufelip.livechat.ui.features.settings.privacy

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.domain.models.BlockedContactsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberBlockedContactsPresenter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BlockedContactsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        BlockedContactsScreen(
            state = previewState(),
            modifier = modifier,
            onBack = onBack,
        )
        return
    }

    val presenter = rememberBlockedContactsPresenter()
    val state by presenter.collectState()

    var showErrorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    val errorMessage = state.errorMessage
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                presenter.clearError()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        presenter.clearError()
                    },
                ) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(strings.general.errorTitle) },
            text = { Text(errorMessage) },
        )
    }

    BlockedContactsScreen(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onUnblock = presenter::unblockContact,
    )
}

private fun previewState(): BlockedContactsUiState =
    BlockedContactsUiState(
        isLoading = false,
        contacts =
            listOf(
                BlockedContact(userId = "user_1", displayName = "Alex Morgan", phoneNumber = "+1 555 123 4567"),
            ),
    )

@DevicePreviews
@Preview
@Composable
private fun BlockedContactsRoutePreview() {
    LiveChatPreviewContainer {
        BlockedContactsRoute()
    }
}
