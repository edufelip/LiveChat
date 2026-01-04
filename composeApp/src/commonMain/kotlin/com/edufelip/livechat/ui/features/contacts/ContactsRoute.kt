package com.edufelip.livechat.ui.features.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import com.edufelip.livechat.contacts.PermissionState
import com.edufelip.livechat.contacts.rememberContactsPermissionManager
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.presentation.ContactsEvent
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberContactsPresenter
import com.edufelip.livechat.ui.util.isUiTestMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ContactsRoute(
    phoneContactsProvider: () -> List<Contact>,
    onContactSelected: (Contact, String) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
    if (LocalInspectionMode.current) {
        ContactsScreen(
            modifier = modifier,
            state = PreviewFixtures.contactsState(strings),
            onInvite = {},
            onContactSelected = {},
            onSync = {},
            onDismissError = {},
        )
        return
    }

    val isUiTest = isUiTestMode()
    val presenter = rememberContactsPresenter()
    val state by presenter.collectState()
    val scope = rememberCoroutineScope()
    val permissionManager = rememberContactsPermissionManager()
    val permissionError = remember { mutableStateOf<String?>(null) }
    val permissionStatus = remember { mutableStateOf(permissionManager.status()) }
    val hasRequestedPermission = remember { mutableStateOf(false) }
    val uiTestContacts = remember { mutableStateOf<List<Contact>>(emptyList()) }
    val uiTestSyncComplete = remember { mutableStateOf(false) }

    val baseState =
        if (isUiTest && uiTestContacts.value.isNotEmpty()) {
            state.copy(
                localContacts = uiTestContacts.value,
                validatedContacts = uiTestContacts.value.filter { it.isRegistered },
                isLoading = false,
                isSyncing = false,
            )
        } else {
            state
        }
    val screenState =
        permissionError.value?.let { message ->
            baseState.copy(errorMessage = message)
        } ?: baseState
    val showSyncButton = permissionStatus.value != PermissionState.Granted

    suspend fun loadContacts(): List<Contact> {
        return if (isUiTest) {
            phoneContactsProvider().ifEmpty { previewContacts }
        } else {
            withContext(Dispatchers.Default) {
                phoneContactsProvider()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isUiTest && !hasRequestedPermission.value && permissionStatus.value != PermissionState.Granted) {
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
            val contacts = loadContacts()
            if (isUiTest) {
                uiTestContacts.value = contacts
                uiTestSyncComplete.value = true
            } else if (presenter.shouldSyncContacts(contacts)) {
                presenter.syncContacts(contacts)
            }
        }
    }

    LaunchedEffect(presenter) {
        presenter.events.collect { event ->
            when (event) {
                is ContactsEvent.OpenConversation -> onContactSelected(event.contact, event.conversationId)
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

    val screenModifier =
        if (isUiTest && uiTestSyncComplete.value) {
            modifier.testTag("contacts_sync_complete")
        } else {
            modifier
        }

    Box {
        ContactsScreen(
            modifier = screenModifier,
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
                            val contacts = loadContacts()
                            if (isUiTest) {
                                uiTestContacts.value = contacts
                                uiTestSyncComplete.value = true
                            } else {
                                presenter.syncContacts(contacts, force = true)
                            }
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
}

@DevicePreviews
@Preview
@Composable
private fun ContactsRoutePreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
        ContactsRoute(
            phoneContactsProvider = { previewContacts },
            onContactSelected = { _, _ -> },
            onShareInvite = {},
        )
    }
}
