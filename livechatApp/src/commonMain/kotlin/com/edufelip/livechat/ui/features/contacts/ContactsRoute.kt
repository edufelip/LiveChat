package com.edufelip.livechat.ui.features.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.contacts.PermissionState
import com.edufelip.livechat.contacts.rememberContactsPermissionManager
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberContactsPresenter
import com.edufelip.livechat.ui.theme.spacing
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.InviteChannel
import com.edufelip.livechat.domain.presentation.ContactsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ContactsRoute(
    phoneContactsProvider: () -> List<Contact>,
    onContactSelected: (Contact) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        ContactsScreen(
            modifier = modifier,
            state = PreviewFixtures.contactsState,
            onInvite = {},
            onContactSelected = {},
            onSync = {},
            onRefresh = {},
            onDismissError = {},
        )
        return
    }

    val presenter = rememberContactsPresenter()
    val state by presenter.collectState()
    val scope = rememberCoroutineScope()
    val permissionManager = rememberContactsPermissionManager()
    val permissionError = remember { mutableStateOf<String?>(null) }
    val permissionStatus = remember { mutableStateOf(permissionManager.status()) }
    val hasRequestedPermission = remember { mutableStateOf(false) }
    var pendingInvite by remember { mutableStateOf<Contact?>(null) }

    val screenState =
        permissionError.value?.let { message ->
            state.copy(errorMessage = message)
        } ?: state
    val showSyncButton = permissionStatus.value != PermissionState.Granted

    LaunchedEffect(Unit) {
        if (!hasRequestedPermission.value && permissionStatus.value != PermissionState.Granted) {
            hasRequestedPermission.value = true
            val result = permissionManager.ensurePermission()
            permissionStatus.value = result
            if (result == PermissionState.Denied) {
                permissionError.value = strings.contacts.permissionDeniedMessage
            }
        }
    }

    LaunchedEffect(permissionStatus.value) {
        if (permissionStatus.value == PermissionState.Granted) {
            permissionError.value = null
            presenter.clearError()
            val contacts =
                withContext(Dispatchers.Default) {
                    phoneContactsProvider()
                }
            presenter.syncContacts(contacts)
        }
    }

    LaunchedEffect(presenter) {
        presenter.events.collect { event ->
            when (event) {
                is ContactsEvent.OpenConversation -> onContactSelected(event.contact)
                is ContactsEvent.ShareInvite ->
                    onShareInvite(
                        InviteShareRequest(
                            contact = event.contact,
                            channel = event.channel,
                            message = event.message,
                        ),
                    )
            }
        }
    }

    if (pendingInvite != null) {
        InviteChannelDialog(
            strings = strings,
            onDismiss = { pendingInvite = null },
            onChannelSelected = { channel ->
                pendingInvite?.let { contact ->
                    presenter.inviteContact(contact, channel)
                }
                pendingInvite = null
            },
        )
    }

    ContactsScreen(
        modifier = modifier,
        state = screenState,
        showSyncButton = showSyncButton,
        onInvite = { pendingInvite = it },
        onContactSelected = { presenter.onContactSelected(it) },
        onSync = {
            scope.launch {
                presenter.clearError()
                permissionError.value = null

                when (permissionManager.ensurePermission()) {
                    PermissionState.Granted -> {
                        val contacts = withContext(Dispatchers.Default) { phoneContactsProvider() }
                        presenter.syncContacts(contacts)
                        permissionStatus.value = PermissionState.Granted
                    }
                    PermissionState.Denied -> {
                        permissionStatus.value = PermissionState.Denied
                        permissionError.value = strings.contacts.permissionDeniedMessage
                    }
                }
            }
        },
        onRefresh = {
            scope.launch {
                val contacts = withContext(Dispatchers.Default) { phoneContactsProvider() }
                presenter.syncContacts(contacts)
            }
        },
        onDismissError = {
            permissionError.value = null
            presenter.clearError()
        },
    )
}

@Composable
private fun InviteChannelDialog(
    strings: LiveChatStrings,
    onDismiss: () -> Unit,
    onChannelSelected: (InviteChannel) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.contacts.inviteDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                InviteChannel.DefaultOptions.forEach { channel ->
                    TextButton(onClick = { onChannelSelected(channel) }) {
                        Text(channel.displayName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.contacts.inviteDialogCancel)
            }
        },
    )
}

@DevicePreviews
@Preview
@Composable
private fun ContactsRoutePreview() {
    LiveChatPreviewContainer {
        ContactsRoute(
            phoneContactsProvider = { PreviewFixtures.contacts },
            onContactSelected = {},
            onShareInvite = {},
        )
    }
}
