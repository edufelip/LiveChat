package com.edufelip.livechat.ui.features.settings.account

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAccountPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBack,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        AccountSettingsScreen(
            modifier = modifier,
            state = previewState(),
            onBack = onBack,
            onEditProfile = {},
            onEditDisplayName = {},
            onEditStatus = {},
            onEditEmail = {},
            onDeleteAccount = {},
        )
        return
    }

    val presenter = rememberAccountPresenter()
    val state by presenter.collectState()
    val sessionProvider = rememberSessionProvider()

    var activeEdit by remember { mutableStateOf(EditField.None) }
    var editValue by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    LaunchedEffect(activeEdit, state.profile) {
        editValue =
            when (activeEdit) {
                EditField.DisplayName -> state.profile?.displayName.orEmpty()
                EditField.StatusMessage -> state.profile?.statusMessage.orEmpty()
                EditField.Email -> state.profile?.email.orEmpty()
                EditField.None -> ""
            }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            signOutPlatformUser()
            sessionProvider.setSession(null)
            onAccountDeleted()
            presenter.acknowledgeDeletion()
        }
    }

    val errorMessage = state.errorMessage
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                presenter.clearError()
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    presenter.clearError()
                }) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(strings.general.errorTitle) },
            text = { Text(errorMessage) },
        )
    }

    if (activeEdit != EditField.None) {
        EditAccountFieldDialog(
            title = activeEdit.title(strings),
            value = editValue,
            onValueChange = { editValue = it },
            onDismiss = { activeEdit = EditField.None },
            onConfirm = {
                when (activeEdit) {
                    EditField.DisplayName -> presenter.updateDisplayName(editValue)
                    EditField.StatusMessage -> presenter.updateStatusMessage(editValue)
                    EditField.Email -> presenter.updateEmail(editValue)
                    EditField.None -> Unit
                }
                activeEdit = EditField.None
            },
            confirmEnabled = activeEdit.canSave(editValue),
            isUpdating = state.isUpdating,
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        presenter.requestDeleteAccount()
                    },
                    enabled = !state.isDeleting,
                ) {
                    Text(strings.account.deleteConfirmCta)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    enabled = !state.isDeleting,
                ) {
                    Text(strings.general.cancel)
                }
            },
            title = { Text(strings.account.deleteConfirmTitle) },
            text = { Text(strings.account.deleteConfirmBody) },
        )
    }

    val allowEdits = !state.isLoading && !state.isUpdating && !state.isDeleting

    AccountSettingsScreen(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onEditProfile = { if (allowEdits) activeEdit = EditField.DisplayName },
        onEditDisplayName = { if (allowEdits) activeEdit = EditField.DisplayName },
        onEditStatus = { if (allowEdits) activeEdit = EditField.StatusMessage },
        onEditEmail = { if (allowEdits) activeEdit = EditField.Email },
        onDeleteAccount = { if (!state.isDeleting) showDeleteConfirm = true },
    )
}

@Composable
private fun EditAccountFieldDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    isUpdating: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled && !isUpdating,
            ) {
                Text(text = liveChatStrings().account.saveCta)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating,
            ) {
                Text(text = liveChatStrings().general.cancel)
            }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

private enum class EditField {
    DisplayName,
    StatusMessage,
    Email,
    None,
}

private fun EditField.title(strings: LiveChatStrings): String =
    when (this) {
        EditField.DisplayName -> strings.account.editDisplayNameTitle
        EditField.StatusMessage -> strings.account.editStatusTitle
        EditField.Email -> strings.account.editEmailTitle
        EditField.None -> ""
    }

private fun EditField.canSave(value: String): Boolean =
    when (this) {
        EditField.DisplayName, EditField.Email -> value.trim().isNotEmpty()
        EditField.StatusMessage, EditField.None -> true
    }

private fun previewState(): AccountUiState =
    AccountUiState(
        isLoading = false,
        profile =
            AccountProfile(
                userId = "preview-user",
                displayName = "Alex Morgan",
                statusMessage = "Available for chat",
                phoneNumber = "+1 (555) 123-4567",
                email = "alex.morgan@example.com",
                photoUrl = null,
            ),
    )

@DevicePreviews
@Preview
@Composable
private fun AccountSettingsRoutePreview() {
    LiveChatPreviewContainer {
        AccountSettingsRoute()
    }
}
