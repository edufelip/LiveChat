package com.edufelip.livechat.ui.features.settings.privacy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.components.atoms.BottomSheetDragHandle
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

data class PrivacyOption(
    val id: LastSeenAudience,
    val label: String,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun PrivacyLastSeenBottomSheet(
    title: String,
    description: String,
    options: List<PrivacyOption>,
    selectedId: LastSeenAudience,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onSelect: (LastSeenAudience) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(bottom = MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column {
                options.forEach { option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option.id) }
                                .padding(vertical = MaterialTheme.spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        RadioButton(
                            selected = option.id == selectedId,
                            onClick = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfirm,
                enabled = confirmEnabled,
            ) {
                Text(confirmLabel)
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacyLastSeenBottomSheetPreview() {
    val strings = liveChatStrings()
    LiveChatPreviewContainer {
        PrivacyLastSeenBottomSheet(
            title = strings.privacy.lastSeenTitle,
            description = strings.privacy.lastSeenSheetDescription,
            options =
                listOf(
                    PrivacyOption(LastSeenAudience.Everyone, strings.privacy.lastSeenEveryone),
                    PrivacyOption(LastSeenAudience.Contacts, strings.privacy.lastSeenContacts),
                    PrivacyOption(LastSeenAudience.Nobody, strings.privacy.lastSeenNobody),
                ),
            selectedId = LastSeenAudience.Contacts,
            confirmLabel = strings.privacy.saveCta,
            confirmEnabled = true,
            onSelect = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}
