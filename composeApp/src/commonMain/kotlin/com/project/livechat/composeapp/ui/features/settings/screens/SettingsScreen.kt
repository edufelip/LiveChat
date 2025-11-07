package com.project.livechat.composeapp.ui.features.settings.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class SettingsSection(val title: String, val description: String) {
    Account("Account", "Profile, status, and linked phone number"),
    Notifications("Notifications", "Mute, schedule quiet hours, sounds"),
    Appearance("Appearance", "Themes, typography scale, accessibility"),
    Privacy("Privacy", "Blocked contacts, invite preferences"),
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsSection) -> Unit = {},
) {
    val sections = remember { SettingsSection.values() }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Text(
            text = "Make LiveChat yours",
            style = MaterialTheme.typography.titleMedium,
        )
        sections.forEach { section ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSectionSelected(section) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun SettingsScreenPreview() {
    LiveChatPreviewContainer {
        SettingsScreen()
    }
}
