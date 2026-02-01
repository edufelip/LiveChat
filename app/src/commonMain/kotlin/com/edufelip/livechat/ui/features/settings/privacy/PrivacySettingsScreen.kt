package com.edufelip.livechat.ui.features.settings.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.components.settingsItemHighlight
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyChevronCard
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyRadioOptionRow
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacySectionCard
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacySettingsHeader
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyToggleCard
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
fun PrivacySettingsScreen(
    state: PrivacySettingsUiState,
    lastSeenSummary: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    targetItemId: String? = null,
    onOpenBlockedContacts: () -> Unit = {},
    onInvitePreferenceSelected: (InvitePreference) -> Unit = {},
    onOpenLastSeen: () -> Unit = {},
    onToggleReadReceipts: (Boolean) -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val privacyStrings = strings.privacy
    val generalStrings = strings.general
    val settings = state.settings
    val allowEdits = !state.isLoading && !state.isUpdating
    val scrollState = rememberScrollState()
    val onBackAction = rememberStableAction(onBack)
    val onOpenBlockedContactsAction = rememberStableAction(onOpenBlockedContacts)
    val onOpenLastSeenAction = rememberStableAction(onOpenLastSeen)
    val onToggleReadReceiptsAction = rememberStableAction(onToggleReadReceipts)
    val onOpenPrivacyPolicyAction = rememberStableAction(onOpenPrivacyPolicy)
    val onInvitePreferenceSelectedAction = rememberStableAction(onInvitePreferenceSelected)
    val inviteOptions =
        remember(privacyStrings) {
            listOf(
                InviteOption(
                    preference = InvitePreference.Everyone,
                    label = privacyStrings.inviteEveryone,
                ),
                InviteOption(
                    preference = InvitePreference.Contacts,
                    label = privacyStrings.inviteContacts,
                ),
                InviteOption(
                    preference = InvitePreference.Nobody,
                    label = privacyStrings.inviteNobody,
                ),
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        PrivacySettingsHeader(
            title = privacyStrings.screenTitle,
            subtitle = privacyStrings.screenSubtitle,
            backContentDescription = generalStrings.dismiss,
            onBack = onBackAction,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("privacy_blocked_contacts", targetItemId)) {
            PrivacyChevronCard(
                title = privacyStrings.blockedContactsTitle,
                subtitle = privacyStrings.blockedContactsSubtitle,
                enabled = allowEdits,
                onClick = onOpenBlockedContactsAction,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("privacy_invite_preferences", targetItemId)) {
            PrivacySectionCard(
                title = privacyStrings.invitePreferencesTitle,
                subtitle = privacyStrings.invitePreferencesSubtitle,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    inviteOptions.forEach { option ->
                        val onClick =
                            remember(option.preference, onInvitePreferenceSelectedAction) {
                                { onInvitePreferenceSelectedAction(option.preference) }
                            }
                        PrivacyRadioOptionRow(
                            label = option.label,
                            selected = settings.invitePreference == option.preference,
                            enabled = allowEdits,
                            onClick = onClick,
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.settingsItemHighlight("privacy_last_seen", targetItemId)) {
            PrivacyChevronCard(
                title = privacyStrings.lastSeenTitle,
                subtitle = lastSeenSummary,
                enabled = allowEdits,
                onClick = onOpenLastSeenAction,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("privacy_read_receipts", targetItemId)) {
            PrivacyToggleCard(
                title = privacyStrings.readReceiptsTitle,
                subtitle = privacyStrings.readReceiptsSubtitle,
                checked = settings.readReceiptsEnabled,
                enabled = allowEdits,
                onCheckedChange = onToggleReadReceiptsAction,
            )
        }

        PrivacyChevronCard(
            title = privacyStrings.privacyPolicyTitle,
            subtitle = privacyStrings.privacyPolicySubtitle,
            enabled = allowEdits,
            onClick = onOpenPrivacyPolicyAction,
        )
    }
}

private data class InviteOption(
    val preference: InvitePreference,
    val label: String,
)

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacySettingsScreenPreview() {
    LiveChatPreviewContainer {
        PrivacySettingsScreen(
            state = PrivacySettingsUiState(isLoading = false),
            lastSeenSummary = liveChatStrings().privacy.lastSeenNobody,
        )
    }
}
