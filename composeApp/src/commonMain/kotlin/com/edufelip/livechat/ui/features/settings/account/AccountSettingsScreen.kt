package com.edufelip.livechat.ui.features.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = strings.general.dismiss,
                )
            }
            Text(
                text = strings.account.screenTitle,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Text(
            text = strings.account.screenSubtitle,
            style = MaterialTheme.typography.titleMedium,
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

        DeleteAccountCard(
            title = strings.account.deleteTitle,
            description = strings.account.deleteDescription,
            onClick = onDeleteAccount,
        )
    }
}

@Composable
private fun AccountProfileCard(
    displayName: String,
    onlineLabel: String,
    initials: String,
    editLabel: String,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = CircleShape,
                                ),
                    )
                }
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = onlineLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onEdit) {
                Text(editLabel)
            }
        }
    }
}

@Composable
private fun AccountFieldCard(
    title: String,
    value: String,
    helper: String? = null,
    onClick: (() -> Unit)?,
) {
    val isEnabled = onClick != null
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (isEnabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (helper != null) {
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint =
                    if (isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
            )
        }
    }
}

@Composable
private fun DeleteAccountCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val errorColor = MaterialTheme.colorScheme.error
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = errorColor,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = errorColor,
            )
        }
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
