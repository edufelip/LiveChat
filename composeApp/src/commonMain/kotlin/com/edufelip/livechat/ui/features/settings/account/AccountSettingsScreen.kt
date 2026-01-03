package com.edufelip.livechat.ui.features.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.account.components.AccountDeleteCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountFieldCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountProfileCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountSettingsHeader
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSettingsScreen(
    state: AccountUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onEditDisplayName: () -> Unit = {},
    onEditStatus: () -> Unit = {},
    onEditEmail: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val profile = state.profile
    val displayName =
        profile?.displayName?.takeIf { it.isNotBlank() }
            ?: strings.account.displayNameMissing
    val statusMessage =
        profile?.statusMessage?.takeIf { it.isNotBlank() }
            ?: strings.account.statusPlaceholder
    val phoneNumber = profile?.phoneNumber?.takeIf { it.isNotBlank() } ?: strings.account.phoneMissing
    val email = profile?.email?.takeIf { it.isNotBlank() } ?: strings.account.emailMissing
    val initials = displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        AccountSettingsHeader(
            title = strings.account.screenTitle,
            subtitle = strings.account.screenSubtitle,
            backContentDescription = strings.general.dismiss,
            onBack = onBack,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AccountProfileCard(
            displayName = displayName,
            onlineLabel = strings.account.onlineLabel,
            initials = initials,
            onEdit = onEditProfile,
            editLabel = strings.account.editCta,
        )

        AccountFieldCard(
            title = strings.account.displayNameLabel,
            value = displayName,
            onClick = onEditDisplayName,
        )

        AccountFieldCard(
            title = strings.account.statusLabel,
            value = statusMessage,
            onClick = onEditStatus,
        )

        AccountFieldCard(
            title = strings.account.phoneLabel,
            value = phoneNumber,
            helper = strings.account.phoneReadOnlyHint,
            onClick = null,
        )

        AccountFieldCard(
            title = strings.account.emailLabel,
            value = email,
            onClick = onEditEmail,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        AccountDeleteCard(
            title = strings.account.deleteTitle,
            description = strings.account.deleteDescription,
            onClick = onDeleteAccount,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun AccountSettingsScreenPreview() {
    LiveChatPreviewContainer {
        AccountSettingsScreen(
            state = AccountUiState(isLoading = false),
        )
    }
}
