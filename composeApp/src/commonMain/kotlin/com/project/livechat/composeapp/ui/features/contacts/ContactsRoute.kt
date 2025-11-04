package com.project.livechat.composeapp.ui.features.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.features.contacts.screens.ContactsScreen
import com.project.livechat.composeapp.ui.state.collectState
import com.project.livechat.composeapp.ui.state.rememberContactsPresenter
import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ContactsRoute(
    phoneContactsProvider: () -> List<Contact>,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        ContactsScreen(
            modifier = modifier,
            state = PreviewFixtures.contactsState,
            onInvite = {},
            onSync = {},
            onDismissError = {},
        )
        return
    }

    val presenter = rememberContactsPresenter()
    val state by presenter.collectState()
    val scope = rememberCoroutineScope()

    ContactsScreen(
        modifier = modifier,
        state = state,
        onInvite = { presenter.inviteContact(it) },
        onSync = {
            scope.launch {
                presenter.syncContacts(phoneContactsProvider())
            }
        },
        onDismissError = { presenter.clearError() },
    )
}

@DevicePreviews
@Preview
@Composable
private fun ContactsRoutePreview() {
    LiveChatPreviewContainer {
        ContactsRoute(phoneContactsProvider = { PreviewFixtures.contacts })
    }
}
