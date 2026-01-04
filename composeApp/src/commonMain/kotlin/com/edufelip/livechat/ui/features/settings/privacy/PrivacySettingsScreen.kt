package com.edufelip.livechat.ui.features.settings.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyChevronCard
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyRadioOptionRow
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacySectionCard
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacySettingsHeader
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyToggleCard
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PrivacySettingsScreen(
    state: PrivacySettingsUiState,
    lastSeenSummary: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenBlockedContacts: () -> Unit = {},
    onInvitePreferenceSelected: (InvitePreference) -> Unit = {},
    onOpenLastSeen: () -> Unit = {},
    onToggleReadReceipts: (Boolean) -> Unit = {},
    onToggleShareUsageData: (Boolean) -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val settings = state.settings
    val allowEdits = !state.isLoading && !state.isUpdating

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        PrivacySettingsHeader(
            title = strings.privacy.screenTitle,
            subtitle = strings.privacy.screenSubtitle,
            backContentDescription = strings.general.dismiss,
            onBack = onBack,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PrivacyChevronCard(
            title = strings.privacy.blockedContactsTitle,
            subtitle = strings.privacy.blockedContactsSubtitle,
            enabled = allowEdits,
            onClick = onOpenBlockedContacts,
        )

        PrivacySectionCard(
            title = strings.privacy.invitePreferencesTitle,
            subtitle = strings.privacy.invitePreferencesSubtitle,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                PrivacyRadioOptionRow(
                    label = strings.privacy.inviteEveryone,
                    selected = settings.invitePreference == InvitePreference.Everyone,
                    enabled = allowEdits,
                    onClick = { onInvitePreferenceSelected(InvitePreference.Everyone) },
                )
                PrivacyRadioOptionRow(
                    label = strings.privacy.inviteContacts,
                    selected = settings.invitePreference == InvitePreference.Contacts,
                    enabled = allowEdits,
                    onClick = { onInvitePreferenceSelected(InvitePreference.Contacts) },
                )
                PrivacyRadioOptionRow(
                    label = strings.privacy.inviteNobody,
                    selected = settings.invitePreference == InvitePreference.Nobody,
                    enabled = allowEdits,
                    onClick = { onInvitePreferenceSelected(InvitePreference.Nobody) },
                )
            }
        }

        PrivacyChevronCard(
            title = strings.privacy.lastSeenTitle,
            subtitle = lastSeenSummary,
            enabled = allowEdits,
            onClick = onOpenLastSeen,
        )

        PrivacyToggleCard(
            title = strings.privacy.readReceiptsTitle,
            subtitle = strings.privacy.readReceiptsSubtitle,
            checked = settings.readReceiptsEnabled,
            enabled = allowEdits,
            onCheckedChange = onToggleReadReceipts,
        )

        PrivacyToggleCard(
            title = strings.privacy.shareUsageDataTitle,
            subtitle = strings.privacy.shareUsageDataSubtitle,
            checked = settings.shareUsageData,
            enabled = allowEdits,
            onCheckedChange = onToggleShareUsageData,
        )

        PrivacyChevronCard(
            title = strings.privacy.privacyPolicyTitle,
            subtitle = strings.privacy.privacyPolicySubtitle,
            enabled = allowEdits,
            onClick = onOpenPrivacyPolicy,
        )
    }
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
