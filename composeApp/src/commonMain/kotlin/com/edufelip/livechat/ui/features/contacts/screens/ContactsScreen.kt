package com.edufelip.livechat.ui.features.contacts.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.components.atoms.Badge
import com.edufelip.livechat.ui.components.atoms.SectionHeader
import com.edufelip.livechat.ui.components.molecules.ErrorBanner
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.components.molecules.RowWithActions
import com.edufelip.livechat.ui.features.contacts.ContactsTestTags
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
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
    val strings = liveChatStrings()
    val hasContacts = state.localContacts.isNotEmpty() || state.validatedContacts.isNotEmpty()

    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
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
        }

        val errorMessage = state.errorMessage
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        ) {
            ErrorBanner(
                modifier = Modifier.fillMaxWidth(),
                message = errorMessage.orEmpty(),
                onDismiss = onDismissError,
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
        verticalArrangement =
            Arrangement.spacedBy(
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
            val merged = linkedMapOf<String, Contact>()
            val validatedMap =
                state.validatedContacts.associateBy { normalizePhoneNumber(it.phoneNo) }
            state.localContacts.forEach { contact ->
                val normalizedPhone = normalizePhoneNumber(contact.phoneNo)
                val validated = validatedMap[normalizedPhone]
                merged[normalizedPhone] =
                    if (validated != null) {
                        contact.copy(isRegistered = true)
                    } else {
                        contact
                    }
            }
            state.validatedContacts.forEach { contact ->
                merged[normalizePhoneNumber(contact.phoneNo)] = contact.copy(isRegistered = true)
            }
            merged.values.sortedBy { it.name.lowercase() }
        }
    val registeredContacts =
        remember(contacts) { contacts.filter { it.isRegistered } }
    val inviteCandidates =
        remember(contacts) { contacts.filterNot { it.isRegistered } }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(ContactsTestTags.LIST),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm / 2),
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

            if (registeredContacts.isNotEmpty()) {
                item {
                    SectionHeader(title = strings.contacts.registeredSectionTitle)
                }
                items(registeredContacts, key = { normalizePhoneNumber(it.phoneNo) }) { contact ->
                    RowWithActions(
                        title = contact.name,
                        subtitle = contact.phoneNo,
                        endContent = {
                            Badge(
                                text = strings.contacts.onLiveChatBadge,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        highlight = true,
                        onClick = { onContactSelected(contact) },
                        enabled = true,
                    )
                }
            }

            if (!state.isSyncing && inviteCandidates.isNotEmpty()) {
                item {
                    SectionHeader(title = strings.contacts.inviteSectionTitle)
                }
                items(inviteCandidates, key = { normalizePhoneNumber(it.phoneNo) }) { contact ->
                    RowWithActions(
                        title = contact.name,
                        subtitle = contact.phoneNo,
                        endContent = {
                            TextButton(
                                onClick = { onInvite(contact) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(strings.contacts.inviteCta)
                            }
                        },
                        highlight = false,
                        onClick = {},
                        enabled = false,
                    )
                }
            } else if (state.isSyncing && inviteCandidates.isNotEmpty()) {
                item {
                    Text(
                        text = strings.contacts.validatingSectionMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.sm),
                    )
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
        val strings = liveChatStrings()
        ContactsScreen(
            state = PreviewFixtures.contactsState(strings),
            onInvite = {},
            onContactSelected = {},
            onSync = {},
            onDismissError = {},
        )
    }
}
