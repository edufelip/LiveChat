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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    val accountStrings = strings.account
    val generalStrings = strings.general
    val scrollState = rememberScrollState()
    val displayData =
        remember(state.profile, accountStrings) {
            val profile = state.profile
            val displayName =
                profile?.displayName?.takeIf { it.isNotBlank() }
                    ?: accountStrings.displayNameMissing
            val statusMessage =
                profile?.statusMessage?.takeIf { it.isNotBlank() }
                    ?: accountStrings.statusPlaceholder
            val phoneNumber =
                profile?.phoneNumber?.takeIf { it.isNotBlank() }
                    ?: accountStrings.phoneMissing
            val email = profile?.email?.takeIf { it.isNotBlank() } ?: accountStrings.emailMissing
            val initials = displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            AccountDisplayData(
                displayName = displayName,
                statusMessage = statusMessage,
                phoneNumber = phoneNumber,
                email = email,
                initials = initials,
            )
        }
    val onBackAction = rememberStableAction(onBack)
    val onEditProfileAction = rememberStableAction(onEditProfile)
    val onEditDisplayNameAction = rememberStableAction(onEditDisplayName)
    val onEditStatusAction = rememberStableAction(onEditStatus)
    val onEditEmailAction = rememberStableAction(onEditEmail)
    val onDeleteAccountAction = rememberStableAction(onDeleteAccount)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        AccountSettingsHeader(
            title = accountStrings.screenTitle,
            subtitle = accountStrings.screenSubtitle,
            backContentDescription = generalStrings.dismiss,
            onBack = onBackAction,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AccountProfileCard(
            displayName = displayData.displayName,
            onlineLabel = accountStrings.onlineLabel,
            initials = displayData.initials,
            onEdit = onEditProfileAction,
            editLabel = accountStrings.editCta,
        )

        AccountFieldCard(
            title = accountStrings.displayNameLabel,
            value = displayData.displayName,
            onClick = onEditDisplayNameAction,
        )

        AccountFieldCard(
            title = accountStrings.statusLabel,
            value = displayData.statusMessage,
            onClick = onEditStatusAction,
        )

        AccountFieldCard(
            title = accountStrings.phoneLabel,
            value = displayData.phoneNumber,
            helper = accountStrings.phoneReadOnlyHint,
            onClick = null,
        )

        AccountFieldCard(
            title = accountStrings.emailLabel,
            value = displayData.email,
            onClick = onEditEmailAction,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        AccountDeleteCard(
            title = accountStrings.deleteTitle,
            description = accountStrings.deleteDescription,
            onClick = onDeleteAccountAction,
        )
    }
}

private data class AccountDisplayData(
    val displayName: String,
    val statusMessage: String,
    val phoneNumber: String,
    val email: String,
    val initials: String,
)

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
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
