package com.edufelip.livechat.ui.features.contacts.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.atoms.Badge
import com.edufelip.livechat.ui.components.molecules.ErrorBanner
import com.edufelip.livechat.ui.components.molecules.LoadingState
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
    onSearchQueryChange: (String) -> Unit,
    onDismissError: () -> Unit,
    onBack: (() -> Unit)? = null,
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
    val onSearchQueryChangeAction = rememberStableAction(onSearchQueryChange)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        ) {
            ContactsHeader(
                title = contactsStrings.screenTitle,
                onBack = onBack,
                backContentDescription = strings.home.backCta,
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.spacing.gutter)
                        .padding(top = MaterialTheme.spacing.lg),
            )
            ContactsSearchField(
                query = state.searchQuery,
                placeholder = contactsStrings.searchPlaceholder,
                onQueryChange = onSearchQueryChangeAction,
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.spacing.gutter)
                        .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.xs)
                        .testTag(ContactsTestTags.SEARCH_FIELD),
            )
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingState(contactsStrings.loading)
                    !hasContacts ->
                        EmptyContactsState(
                            strings = contactsStrings,
                            showSyncButton = showSyncButton,
                            isSyncing = state.isSyncing,
                            onSync = onSyncClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    else ->
                        ContactsListContent(
                            localContacts = state.localContacts,
                            validatedContacts = state.validatedContacts,
                            searchQuery = state.searchQuery,
                            isSyncing = state.isSyncing,
                            showSyncButton = showSyncButton,
                            onSync = onSyncClick,
                            onInvite = onInvite,
                            onContactSelected = onContactSelected,
                            strings = contactsStrings,
                            modifier = Modifier.fillMaxSize(),
                        )
                }
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
private fun ContactsHeader(
    title: String,
    onBack: (() -> Unit)?,
    backContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = AppIcons.back,
                    contentDescription = backContentDescription,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ContactsSearchField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun EmptyContactsState(
    strings: ContactsStrings,
    showSyncButton: Boolean,
    isSyncing: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = strings.emptyState,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

        if (showSyncButton) {
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
        } else if (isSyncing) {
            Text(
                text = strings.syncing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ContactsListContent(
    localContacts: List<Contact>,
    validatedContacts: List<Contact>,
    searchQuery: String,
    isSyncing: Boolean,
    showSyncButton: Boolean,
    onSync: () -> Unit,
    onInvite: (Contact) -> Unit,
    onContactSelected: (Contact) -> Unit,
    strings: ContactsStrings,
    modifier: Modifier = Modifier,
) {
    val contacts =
        remember(localContacts, validatedContacts) {
            val merged = linkedMapOf<String, Contact>()
            val validatedMap =
                validatedContacts
                    .filter { it.firebaseUid?.isNotBlank() == true }
                    .associateBy { normalizePhoneNumber(it.phoneNo) }
            localContacts.forEach { contact ->
                val normalizedPhone = normalizePhoneNumber(contact.phoneNo)
                val validated = validatedMap[normalizedPhone]
                val shouldHighlight = contact.isRegistered && !contact.firebaseUid.isNullOrBlank()
                merged[normalizedPhone] =
                    if (validated != null) {
                        contact.copy(isRegistered = true, firebaseUid = validated.firebaseUid)
                    } else if (shouldHighlight) {
                        contact
                    } else {
                        contact.copy(isRegistered = false)
                    }
            }
            validatedMap.forEach { (normalizedPhone, contact) ->
                merged[normalizedPhone] = contact.copy(isRegistered = true)
            }
            merged.entries
                .map { (normalizedPhone, contact) -> ContactEntry(normalizedPhone, contact) }
                .sortedBy { it.contact.name.lowercase() }
        }

    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase() }
    val filteredContacts =
        remember(contacts, normalizedQuery) {
            if (normalizedQuery.isBlank()) {
                contacts
            } else {
                contacts.filter { entry ->
                    entry.contact.name.contains(normalizedQuery, ignoreCase = true) ||
                        entry.contact.phoneNo.contains(normalizedQuery, ignoreCase = true) ||
                        normalizePhoneNumber(entry.contact.phoneNo).contains(normalizedQuery)
                }
            }
        }
    val registeredContacts =
        remember(filteredContacts) {
            filteredContacts.filter { entry ->
                entry.contact.isRegistered && !entry.contact.firebaseUid.isNullOrBlank()
            }
        }
    val inviteCandidates =
        remember(filteredContacts) {
            filteredContacts.filterNot { entry ->
                entry.contact.isRegistered && !entry.contact.firebaseUid.isNullOrBlank()
            }
        }
    val showSearchEmptyState = normalizedQuery.isNotBlank() && filteredContacts.isEmpty()

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(ContactsTestTags.LIST),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        contentPadding =
            PaddingValues(
                start = MaterialTheme.spacing.md,
                end = MaterialTheme.spacing.md,
                top = MaterialTheme.spacing.xxs,
                bottom = MaterialTheme.spacing.xxxl,
            ),
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
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text(if (isSyncing) strings.syncing else strings.syncCta)
                }
            }
        }

        if (showSearchEmptyState) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = strings.searchEmptyState,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (registeredContacts.isNotEmpty()) {
            item {
                ContactsSectionHeader(title = strings.registeredSectionTitle)
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
                ContactsSectionHeader(title = strings.inviteSectionTitle)
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

@Composable
private fun ContactsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        style =
            MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.sm),
    )
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

    ContactCard(
        title = entry.contact.name,
        subtitle = entry.contact.phoneNo,
        highlight = true,
        onClick = onClick,
        enabled = true,
        endContent = endContent,
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
                    modifier = Modifier.heightIn(min = 40.dp),
                ) {
                    Text(inviteLabel)
                }
            }
        }

    ContactCard(
        title = entry.contact.name,
        subtitle = entry.contact.phoneNo,
        highlight = false,
        onClick = NoOpClick,
        enabled = false,
        endContent = endContent,
        modifier = modifier,
    )
}

@Composable
private fun ContactCard(
    title: String,
    subtitle: String,
    highlight: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (highlight) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(enabled = enabled, onClick = onClick),
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (highlight) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            endContent()
        }
    }
}

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
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
            onSearchQueryChange = {},
            onDismissError = {},
            onBack = {},
        )
    }
}
