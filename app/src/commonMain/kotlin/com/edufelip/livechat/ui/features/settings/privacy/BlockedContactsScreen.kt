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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.models.BlockedContactsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.privacy.components.BlockedContactRow
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacySettingsHeader
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
fun BlockedContactsScreen(
    state: BlockedContactsUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onUnblock: (String) -> Unit = {},
) {
    val strings = liveChatStrings()
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
            title = strings.privacy.blockedContactsTitle,
            subtitle = null,
            backContentDescription = strings.general.dismiss,
            onBack = onBack,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!state.isLoading && state.contacts.isEmpty()) {
            Text(
                text = strings.privacy.blockedContactsEmpty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.contacts.forEach { contact ->
            BlockedContactRow(
                contact = contact,
                fallbackLabel = strings.privacy.blockedContactUnknown,
                unblockLabel = strings.privacy.unblockCta,
                enabled = allowEdits,
                onUnblock = onUnblock,
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun BlockedContactsScreenPreview() {
    LiveChatPreviewContainer {
        BlockedContactsScreen(
            state = BlockedContactsUiState(isLoading = false),
        )
    }
}
