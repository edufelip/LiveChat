package com.project.livechat.composeapp.ui.features.contacts

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
import com.project.livechat.composeapp.contacts.PermissionState
import com.project.livechat.composeapp.contacts.rememberContactsPermissionManager
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.features.contacts.model.InviteShareRequest
import com.project.livechat.composeapp.ui.features.contacts.screens.ContactsScreen
import com.project.livechat.composeapp.ui.state.collectState
import com.project.livechat.composeapp.ui.state.rememberContactsPresenter
import com.project.livechat.composeapp.ui.theme.spacing
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.InviteChannel
import com.project.livechat.domain.presentation.ContactsEvent
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
                permissionError.value = "Enable contacts permission to sync your phonebook."
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
                        permissionError.value = "Enable contacts permission to sync your phonebook."
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
    onDismiss: () -> Unit,
    onChannelSelected: (InviteChannel) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite via") },
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
                Text("Cancel")
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
