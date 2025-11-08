package com.edufelip.livechat.ui.features.contacts.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.components.atoms.Badge
import com.edufelip.livechat.ui.components.molecules.ErrorBanner
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.components.molecules.RowWithActions
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
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
    val strings = liveChatStrings()
    val hasContacts = state.localContacts.isNotEmpty() || state.validatedContacts.isNotEmpty()
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
                state.isLoading -> LoadingState(strings.contacts.loading)
                !hasContacts -> EmptyContactsState(strings, showSyncButton, state.isSyncing, onSync)
                else ->
                    ContactsListContent(
                        state = state,
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
    strings: LiveChatStrings,
    showSyncButton: Boolean,
    isSyncing: Boolean,
    onSync: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            MaterialTheme.spacing.md,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = strings.contacts.emptyState,
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
                                    stateDescription = strings.contacts.syncingStateDescription
                                }
                            },
            ) {
                Text(if (isSyncing) strings.contacts.syncing else strings.contacts.syncCta)
            }
        } else if (isSyncing) {
            Text(
                text = strings.contacts.syncing,
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
    showSyncButton: Boolean,
    onSync: () -> Unit,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
) {
    val strings = liveChatStrings()
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
                                    stateDescription = strings.contacts.syncingStateDescription
                                }
                            },
                ) {
                    Text(if (state.isSyncing) strings.contacts.syncing else strings.contacts.syncCta)
                }
            }
        }

        items(contacts, key = { it.phoneNo }) { contact ->
            val isValidated = state.validatedContacts.any { it.phoneNo == contact.phoneNo }
            RowWithActions(
                title = contact.name,
                subtitle = contact.phoneNo,
                endContent = {
                    when {
                        isValidated ->
                            Badge(
                                text = strings.contacts.onLiveChatBadge,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        else ->
                            TextButton(
                                onClick = { onInvite(contact) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(strings.contacts.inviteCta)
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
