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
import androidx.compose.runtime.rememberUpdatedState
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
import com.edufelip.livechat.ui.resources.ContactsStrings
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
    val contactsStrings = strings.contacts
    val hasContacts =
        remember(state.localContacts, state.validatedContacts) {
            state.localContacts.isNotEmpty() || state.validatedContacts.isNotEmpty()
        }
    val onSyncClick = rememberStableAction(onSync)
    val onDismissErrorClick = rememberStableAction(onDismissError)

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
                state.isLoading -> LoadingState(contactsStrings.loading)
                !hasContacts ->
                    EmptyContactsState(
                        strings = contactsStrings,
                        showSyncButton = showSyncButton,
                        isSyncing = state.isSyncing,
                        onSync = onSyncClick,
                    )
                else ->
                    ContactsListContent(
                        localContacts = state.localContacts,
                        validatedContacts = state.validatedContacts,
                        isSyncing = state.isSyncing,
                        showSyncButton = showSyncButton,
                        onSync = onSyncClick,
                        onInvite = onInvite,
                        onContactSelected = onContactSelected,
                        strings = contactsStrings,
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
                onDismiss = onDismissErrorClick,
            )
        }
    }
}

@Composable
private fun EmptyContactsState(
    strings: ContactsStrings,
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
            text = strings.emptyState,
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
                                stateDescription = strings.syncingStateDescription
                            }
                        },
            ) {
                Text(if (isSyncing) strings.syncing else strings.syncCta)
            }
        } else if (isSyncing) {
            Text(
                text = strings.syncing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ContactsListContent(
    localContacts: List<Contact>,
    validatedContacts: List<Contact>,
    isSyncing: Boolean,
    showSyncButton: Boolean,
    onSync: () -> Unit,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
    strings: ContactsStrings,
) {
    val contacts =
        remember(localContacts, validatedContacts) {
            val merged = linkedMapOf<String, Contact>()
            val validatedMap =
                validatedContacts.associateBy { normalizePhoneNumber(it.phoneNo) }
            localContacts.forEach { contact ->
                val normalizedPhone = normalizePhoneNumber(contact.phoneNo)
                val validated = validatedMap[normalizedPhone]
                merged[normalizedPhone] =
                    if (validated != null) {
                        contact.copy(isRegistered = true)
                    } else {
                        contact
                    }
            }
            validatedContacts.forEach { contact ->
                merged[normalizePhoneNumber(contact.phoneNo)] = contact.copy(isRegistered = true)
            }
            merged.entries
                .map { (normalizedPhone, contact) -> ContactEntry(normalizedPhone, contact) }
                .sortedBy { it.contact.name.lowercase() }
        }
    val registeredContacts =
        remember(contacts) { contacts.filter { it.contact.isRegistered } }
    val inviteCandidates =
        remember(contacts) { contacts.filterNot { it.contact.isRegistered } }

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
                        enabled = !isSyncing,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .semantics {
                                    if (isSyncing) {
                                        stateDescription = strings.syncingStateDescription
                                    }
                                },
                    ) {
                        Text(if (isSyncing) strings.syncing else strings.syncCta)
                    }
                }
            }

            if (registeredContacts.isNotEmpty()) {
                item {
                    SectionHeader(title = strings.registeredSectionTitle)
                }
                items(registeredContacts, key = { it.normalizedPhone }) { entry ->
                    RegisteredContactRow(
                        entry = entry,
                        badgeText = strings.onLiveChatBadge,
                        onContactSelected = onContactSelected,
                    )
                }
            }

            if (!isSyncing && inviteCandidates.isNotEmpty()) {
                item {
                    SectionHeader(title = strings.inviteSectionTitle)
                }
                items(inviteCandidates, key = { it.normalizedPhone }) { entry ->
                    InviteContactRow(
                        entry = entry,
                        inviteLabel = strings.inviteCta,
                        onInvite = onInvite,
                    )
                }
            } else if (isSyncing && inviteCandidates.isNotEmpty()) {
                item {
                    Text(
                        text = strings.validatingSectionMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisteredContactRow(
    entry: ContactEntry,
    badgeText: String,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onClick = rememberContactAction(entry.contact, onContactSelected)
    val endContent: @Composable () -> Unit =
        remember(badgeText) {
            {
                Badge(
                    text = badgeText,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

    RowWithActions(
        title = entry.contact.name,
        subtitle = entry.contact.phoneNo,
        endContent = endContent,
        highlight = true,
        onClick = onClick,
        enabled = true,
        modifier = modifier,
    )
}

@Composable
private fun InviteContactRow(
    entry: ContactEntry,
    inviteLabel: String,
    onInvite: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onInviteClick = rememberContactAction(entry.contact, onInvite)
    val endContent: @Composable () -> Unit =
        remember(inviteLabel, onInviteClick) {
            {
                TextButton(
                    onClick = onInviteClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(inviteLabel)
                }
            }
        }

    RowWithActions(
        title = entry.contact.name,
        subtitle = entry.contact.phoneNo,
        endContent = endContent,
        highlight = false,
        onClick = NoOpClick,
        enabled = false,
        modifier = modifier,
    )
}

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@Composable
private fun rememberContactAction(
    contact: Contact,
    action: (Contact) -> Unit,
): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember(contact) { { actionState.value(contact) } }
}

private data class ContactEntry(
    val normalizedPhone: String,
    val contact: Contact,
)

private val NoOpClick: () -> Unit = {}

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
