package com.project.livechat.composeapp.ui.features.contacts.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.components.atoms.Badge
import com.project.livechat.composeapp.ui.components.molecules.ErrorBanner
import com.project.livechat.composeapp.ui.components.molecules.LoadingState
import com.project.livechat.composeapp.ui.components.molecules.RowWithActions
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ContactsScreen(
    state: ContactsUiState,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
    onSync: () -> Unit,
    onDismissError: () -> Unit,
    showSyncButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val hasContacts = state.localContacts.isNotEmpty() || state.validatedContacts.isNotEmpty()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.errorMessage?.let { message ->
            ErrorBanner(message = message, onDismiss = onDismissError)
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            when {
                state.isLoading -> LoadingState("Loading contacts…")
                !hasContacts ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No contacts synced yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )

                        if (showSyncButton) {
                            Button(onClick = onSync, enabled = !state.isSyncing) {
                                Text(if (state.isSyncing) "Syncing…" else "Sync Contacts")
                            }
                        } else if (state.isSyncing) {
                            Text(
                                text = "Syncing…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (showSyncButton) {
                            Button(onClick = onSync, enabled = !state.isSyncing) {
                                Text(if (state.isSyncing) "Syncing…" else "Sync Contacts")
                            }
                        }

                        val contacts =
                            remember(state.localContacts, state.validatedContacts) {
                                (state.localContacts + state.validatedContacts)
                                    .distinctBy { it.phoneNo }
                                    .sortedBy { it.name.lowercase() }
                            }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            items(contacts, key = { it.phoneNo }) { contact ->
                                val isValidated = state.validatedContacts.any { it.phoneNo == contact.phoneNo }
                                RowWithActions(
                                    title = contact.name,
                                    subtitle = contact.phoneNo,
                                    endContent = {
                                        if (isValidated) {
                                            Badge(text = "On LiveChat", tint = MaterialTheme.colorScheme.primary)
                                        } else {
                                            TextButton(onClick = { onInvite(contact) }) {
                                                Text("Invite")
                                            }
                                        }
                                    },
                                    highlight = isValidated,
                                    onClick = { if (isValidated) onContactSelected(contact) },
                                    enabled = isValidated,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun ContactsScreenPreview() {
    LiveChatPreviewContainer {
        ContactsScreen(
            state = PreviewFixtures.contactsState,
            onInvite = {},
            onContactSelected = {},
            onSync = {},
            onDismissError = {},
        )
    }
}
