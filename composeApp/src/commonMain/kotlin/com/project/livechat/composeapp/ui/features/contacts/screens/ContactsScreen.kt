package com.project.livechat.composeapp.ui.features.contacts.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.components.atoms.Badge
import com.project.livechat.composeapp.ui.components.atoms.SectionHeader
import com.project.livechat.composeapp.ui.components.molecules.ErrorBanner
import com.project.livechat.composeapp.ui.components.molecules.LoadingState
import com.project.livechat.composeapp.ui.components.molecules.RowWithActions
import com.project.livechat.composeapp.ui.theme.spacing
import com.project.livechat.composeapp.ui.util.formatAsTime
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.models.InviteHistoryItem
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ContactsScreen(
    state: ContactsUiState,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
    onSync: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    showSyncButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val hasContacts = state.localContacts.isNotEmpty() || state.validatedContacts.isNotEmpty()
    val invitedPhones =
        remember(state.inviteHistory) { state.inviteHistory.map { it.contact.phoneNo }.toSet() }
    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = state.isSyncing,
            onRefresh = onRefresh,
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        state.errorMessage?.let { message ->
            ErrorBanner(message = message, onDismiss = onDismissError)
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
        ) {
            when {
                state.isLoading -> LoadingState("Loading contacts…")
                !hasContacts -> EmptyContactsState(showSyncButton, state.isSyncing, onSync)
                else ->
                    ContactsListContent(
                        state = state,
                        invitedPhones = invitedPhones,
                        showSyncButton = showSyncButton,
                        onSync = onSync,
                        onInvite = onInvite,
                        onContactSelected = onContactSelected,
                    )
            }

            PullRefreshIndicator(
                refreshing = state.isSyncing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun EmptyContactsState(
    showSyncButton: Boolean,
    isSyncing: Boolean,
    onSync: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No contacts synced yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )

        if (showSyncButton) {
            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier =
                    Modifier
                        .heightIn(min = 48.dp)
                        .semantics {
                            if (isSyncing) {
                                stateDescription = "Syncing contacts"
                            }
                        },
            ) {
                Text(if (isSyncing) "Syncing…" else "Sync Contacts")
            }
        } else if (isSyncing) {
            Text(
                text = "Syncing…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ContactsListContent(
    state: ContactsUiState,
    invitedPhones: Set<String>,
    showSyncButton: Boolean,
    onSync: () -> Unit,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
) {
    val contacts =
        remember(state.localContacts, state.validatedContacts) {
            (state.localContacts + state.validatedContacts)
                .distinctBy { it.phoneNo }
                .sortedBy { it.name.lowercase() }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
    ) {
        if (showSyncButton) {
            item {
                Button(
                    onClick = onSync,
                    enabled = !state.isSyncing,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics {
                                if (state.isSyncing) {
                                    stateDescription = "Syncing contacts"
                                }
                            },
                ) {
                    Text(if (state.isSyncing) "Syncing…" else "Sync Contacts")
                }
            }
        }

        items(contacts, key = { it.phoneNo }) { contact ->
            val isValidated = state.validatedContacts.any { it.phoneNo == contact.phoneNo }
            val invited = invitedPhones.contains(contact.phoneNo)
            RowWithActions(
                title = contact.name,
                subtitle = contact.phoneNo,
                endContent = {
                    when {
                        isValidated -> Badge(text = "On LiveChat", tint = MaterialTheme.colorScheme.primary)
                        invited -> Badge(text = "Invited", tint = MaterialTheme.colorScheme.tertiary)
                        else ->
                            TextButton(
                                onClick = { onInvite(contact) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text("Invite")
                            }
                    }
                },
                highlight = isValidated,
                onClick = { if (isValidated) onContactSelected(contact) },
                enabled = isValidated,
            )
        }

        if (state.inviteHistory.isNotEmpty()) {
            item { SectionHeader(title = "Invite history") }
            items(state.inviteHistory, key = { it.id }) { record ->
                InviteHistoryRow(record)
            }
        }
    }
}

@Composable
private fun InviteHistoryRow(record: InviteHistoryItem) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            text = record.contact.name.ifBlank { record.contact.phoneNo },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = "${record.channel.displayName} • ${record.timestamp.formatAsTime()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider()
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
            onRefresh = {},
            onDismissError = {},
        )
    }
}
