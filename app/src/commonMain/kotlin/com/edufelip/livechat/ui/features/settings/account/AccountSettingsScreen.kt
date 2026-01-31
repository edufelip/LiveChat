package com.edufelip.livechat.ui.features.settings.account

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
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.account.components.AccountDeleteCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountFieldCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountProfileCard
import com.edufelip.livechat.ui.features.settings.account.components.AccountSettingsHeader
import com.edufelip.livechat.ui.features.settings.components.settingsItemHighlight
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSettingsScreen(
    state: AccountUiState,
    modifier: Modifier = Modifier,
    phoneNumberOverride: String? = null,
    targetItemId: String? = null,
    onBack: () -> Unit = {},
    onEditDisplayName: () -> Unit = {},
    onEditPhoto: () -> Unit = {},
    onEditStatus: () -> Unit = {},
    onEditEmail: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val accountStrings = strings.account
    val generalStrings = strings.general
    val scrollState = rememberScrollState()
    val displayData =
        remember(state.profile, accountStrings, phoneNumberOverride) {
            val profile = state.profile
            val displayName =
                profile?.displayName?.takeIf { it.isNotBlank() }
                    ?: accountStrings.displayNameMissing
            val statusMessage =
                profile?.statusMessage?.takeIf { it.isNotBlank() }
                    ?: accountStrings.statusPlaceholder
            val phoneNumber =
                phoneNumberOverride
                    ?: profile?.phoneNumber?.takeIf { it.isNotBlank() }
                    ?: accountStrings.phoneMissing
            val email = profile?.email?.takeIf { it.isNotBlank() } ?: accountStrings.emailMissing
            val initials = displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            val photoUrl = profile?.photoUrl?.takeIf { it.isNotBlank() }
            val photoValue =
                if (photoUrl.isNullOrBlank()) {
                    accountStrings.photoMissing
                } else {
                    accountStrings.photoChange
                }
            AccountDisplayData(
                displayName = displayName,
                statusMessage = statusMessage,
                phoneNumber = phoneNumber,
                email = email,
                initials = initials,
                photoUrl = photoUrl,
                photoValue = photoValue,
            )
        }
    val onBackAction = rememberStableAction(onBack)
    val onEditDisplayNameAction = rememberStableAction(onEditDisplayName)
    val onEditPhotoAction = rememberStableAction(onEditPhoto)
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
            photoUrl = displayData.photoUrl,
            onClick = onEditDisplayNameAction,
        )

        Box(modifier = Modifier.settingsItemHighlight("account_photo", targetItemId)) {
            AccountFieldCard(
                title = accountStrings.photoLabel,
                value = displayData.photoValue,
                onClick = onEditPhotoAction,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("account_display_name", targetItemId)) {
            AccountFieldCard(
                title = accountStrings.displayNameLabel,
                value = displayData.displayName,
                onClick = onEditDisplayNameAction,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("account_status", targetItemId)) {
            AccountFieldCard(
                title = accountStrings.statusLabel,
                value = displayData.statusMessage,
                onClick = onEditStatusAction,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("account_phone", targetItemId)) {
            AccountFieldCard(
                title = accountStrings.phoneLabel,
                value = displayData.phoneNumber,
                helper = accountStrings.phoneReadOnlyHint,
                onClick = null,
                showChevron = false,
            )
        }

        Box(modifier = Modifier.settingsItemHighlight("account_email", targetItemId)) {
            AccountFieldCard(
                title = accountStrings.emailLabel,
                value = displayData.email,
                onClick = onEditEmailAction,
            )
        }

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
    val photoUrl: String?,
    val photoValue: String,
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
