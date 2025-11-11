package com.edufelip.livechat.ui.features.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.contacts.PermissionState
import com.edufelip.livechat.contacts.rememberContactsPermissionManager
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberContactsPresenter
import com.edufelip.livechat.domain.models.Contact
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
            if (presenter.shouldSyncContacts(contacts)) {
                presenter.syncContacts(contacts)
            }
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
                            message = event.message,
                            chooserTitle = strings.contacts.inviteShareTitle,
                            unavailableMessage = strings.contacts.inviteShareUnavailable,
                        ),
                    )
            }
        }
    }

    ContactsScreen(
        modifier = modifier,
        state = screenState,
        showSyncButton = showSyncButton,
        onInvite = { presenter.inviteContact(it) },
        onContactSelected = { presenter.onContactSelected(it) },
        onSync = {
            scope.launch {
                presenter.clearError()
                permissionError.value = null

                when (permissionManager.ensurePermission()) {
                    PermissionState.Granted -> {
                        val contacts = withContext(Dispatchers.Default) { phoneContactsProvider() }
                        presenter.syncContacts(contacts, force = true)
                        permissionStatus.value = PermissionState.Granted
                    }
                    PermissionState.Denied -> {
                        permissionStatus.value = PermissionState.Denied
                        permissionError.value = strings.contacts.permissionDeniedMessage
                    }
                }
            }
        },
        onDismissError = {
            permissionError.value = null
            presenter.clearError()
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
