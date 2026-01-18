package com.edufelip.livechat.ui.features.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.SettingsTestTags
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.platform.appVersionInfo
import com.edufelip.livechat.ui.platform.isAndroid
import com.edufelip.livechat.ui.platform.openWebViewUrl
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.SettingsStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import com.edufelip.livechat.ui.util.FuzzyMatcher
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class SettingsSection {
    Account,
    Notifications,
    Appearance,
    Privacy,
}

fun SettingsSection.title(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountTitle
        SettingsSection.Notifications -> strings.notificationsTitle
        SettingsSection.Appearance -> strings.appearanceTitle
        SettingsSection.Privacy -> strings.privacyTitle
    }

fun SettingsSection.description(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountDescription
        SettingsSection.Notifications -> strings.notificationsDescription
        SettingsSection.Appearance -> strings.appearanceDescription
        SettingsSection.Privacy -> strings.privacyDescription
    }

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsNavigationRequest) -> Unit = {},
) {
    val strings = liveChatStrings()
    val settingsStrings = strings.settings
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    val allSearchableItems = remember(strings) { buildAllSearchableItems(strings) }
    val onSectionSelectedAction = rememberStableAction(onSectionSelected)
    val normalizedQuery = remember(searchQuery) { searchQuery.trim() }
    val filteredItems =
        remember(allSearchableItems, normalizedQuery) {
            if (normalizedQuery.isBlank()) {
                // Show only top-level sections when not searching
                allSearchableItems.filterIsInstance<SettingsSearchableItem.Section>()
            } else {
                // Search both sections and subitems with fuzzy matching
                allSearchableItems.filter { item ->
                    // Exact match (substring)
                    item.title.contains(normalizedQuery, ignoreCase = true) ||
                        item.keywords.any { keyword ->
                            keyword.contains(normalizedQuery, ignoreCase = true)
                        } ||
                        // Fuzzy match (typo tolerance)
                        FuzzyMatcher.matches(normalizedQuery, item.title) ||
                        item.keywords.any { keyword ->
                            FuzzyMatcher.matches(normalizedQuery, keyword)
                        }
                }
            }
        }
    val rows =
        remember(filteredItems, onSectionSelectedAction, strings) {
            filteredItems.mapNotNull { item ->
                when (item) {
                    is SettingsSearchableItem.Section ->
                        SettingsRowItem(
                            id = item.id,
                            title = item.title,
                            icon = item.navigationRequest.icon(),
                            iconBackground = item.navigationRequest.iconBackground(),
                            trailingIcon = Icons.Rounded.ChevronRight,
                            onClick = {
                                searchQuery = ""
                                onSectionSelectedAction(item.navigationRequest)
                            },
                            parentLabel = null,
                        )

                    is SettingsSearchableItem.SubItem -> {
                        val parentTitle = item.parentSection.title(strings.settings)
                        SettingsRowItem(
                            id = item.id,
                            title = item.title,
                            icon = getSectionIcon(item.parentSection),
                            iconBackground = getSectionIconBackground(item.parentSection),
                            trailingIcon = Icons.Rounded.ChevronRight,
                            onClick = {
                                searchQuery = ""
                                val navRequest =
                                    SettingsNavigationRequest(
                                        section = item.parentSection,
                                        title = parentTitle,
                                        description = item.parentSection.description(strings.settings),
                                        placeholderMessage =
                                            buildOpeningSectionMessage(
                                                strings.settings.openingSectionTemplate,
                                                parentTitle,
                                            ),
                                        targetItemId = item.id,
                                    )
                                onSectionSelectedAction(navRequest)
                            },
                            parentLabel = parentTitle,
                        )
                    }
                }
            }
        }
    val privacyPolicyRow =
        remember(settingsStrings.privacyPolicyTitle, settingsStrings.privacyPolicyUrl, normalizedQuery) {
            val shouldShow =
                normalizedQuery.isBlank() ||
                    settingsStrings.privacyPolicyTitle.contains(normalizedQuery, ignoreCase = true)
            if (shouldShow) {
                SettingsRowItem(
                    id = "privacy_policy",
                    title = settingsStrings.privacyPolicyTitle,
                    icon = Icons.Rounded.Shield,
                    iconBackground = SettingsIconGray,
                    trailingIcon = Icons.Rounded.OpenInNew,
                    onClick = { openWebViewUrl(settingsStrings.privacyPolicyUrl) },
                    parentLabel = null,
                )
            } else {
                null
            }
        }
    val appVersion = remember { appVersionInfo() }
    val appName =
        remember(settingsStrings) {
            if (isAndroid()) settingsStrings.footerAppNameAndroid else settingsStrings.footerAppNameIos
        }
    val versionLabel =
        remember(settingsStrings, appVersion) {
            formatVersionLabel(settingsStrings.versionFormat, appVersion.versionName, appVersion.buildNumber)
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {
            SettingsHeader(title = settingsStrings.screenTitle)
            SettingsSearchField(
                value = searchQuery,
                placeholder = settingsStrings.searchPlaceholder,
                onValueChange = { searchQuery = it },
                modifier = Modifier.testTag(SettingsTestTags.SEARCH_FIELD),
            )

            if (rows.isNotEmpty()) {
                SettingsGroupCard(rows = rows)
            }

            if (privacyPolicyRow != null) {
                SettingsGroupCard(rows = listOf(privacyPolicyRow))
            }

            // Show empty state when searching with no results
            if (normalizedQuery.isNotBlank() && rows.isEmpty() && privacyPolicyRow == null) {
                SettingsEmptySearchState(
                    query = normalizedQuery,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.xl),
                )
            }

            SettingsFooter(
                appName = appName,
                versionLabel = versionLabel,
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
private fun SettingsSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    TextField(
        value = value,
        onValueChange = onValueChange,
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
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
        colors = colors,
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
private fun SettingsGroupCard(
    rows: List<SettingsRowItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                SettingsRow(item = row)
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = MaterialTheme.spacing.md),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsRowItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = item.onClick)
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconTile(
            icon = item.icon,
            backgroundColor = item.iconBackground,
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            if (item.parentLabel != null) {
                Text(
                    text = "${item.parentLabel} › ${item.title}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = item.trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsIconTile(
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(30.dp)
                .background(backgroundColor, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsEmptySearchState(
    query: String,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            text = strings.settings.searchNoResults,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = strings.settings.searchNoResultsHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun SettingsFooter(
    appName: String,
    versionLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = versionLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

private data class SettingsRowItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val trailingIcon: ImageVector,
    val onClick: () -> Unit,
    val parentLabel: String? = null,
)

private sealed interface SettingsSearchableItem {
    val id: String
    val title: String
    val parentSection: SettingsSection?
    val keywords: List<String>

    data class Section(
        override val id: String,
        override val title: String,
        override val parentSection: SettingsSection? = null,
        override val keywords: List<String>,
        val navigationRequest: SettingsNavigationRequest,
    ) : SettingsSearchableItem

    data class SubItem(
        override val id: String,
        override val title: String,
        override val parentSection: SettingsSection,
        override val keywords: List<String>,
    ) : SettingsSearchableItem
}

private fun buildSectionRequests(strings: SettingsStrings): List<SettingsNavigationRequest> {
    val sections = SettingsSection.values()
    return sections.map { section ->
        val title = section.title(strings)
        val description = section.description(strings)
        val message = buildOpeningSectionMessage(strings.openingSectionTemplate, title)
        SettingsNavigationRequest(
            section = section,
            title = title,
            description = description,
            placeholderMessage = message,
        )
    }
}

private fun SettingsNavigationRequest.icon(): ImageVector =
    when (section) {
        SettingsSection.Account -> Icons.Rounded.Person
        SettingsSection.Notifications -> Icons.Rounded.Notifications
        SettingsSection.Appearance -> Icons.Rounded.DarkMode
        SettingsSection.Privacy -> Icons.Rounded.Lock
    }

private fun SettingsNavigationRequest.iconBackground(): Color =
    when (section) {
        SettingsSection.Account -> SettingsIconBlue
        SettingsSection.Notifications -> SettingsIconRed
        SettingsSection.Appearance -> SettingsIconPurple
        SettingsSection.Privacy -> SettingsIconGreen
    }

private fun buildOpeningSectionMessage(
    template: String,
    title: String,
): String =
    template
        .replace("%1\$s", title)
        .replace("%s", title)

private fun formatVersionLabel(
    template: String,
    versionName: String,
    buildNumber: String,
): String =
    template
        .replace("%1\$s", versionName)
        .replace("%2\$s", buildNumber)
        .replace("%s", versionName)

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

private fun buildAllSearchableItems(strings: LiveChatStrings): List<SettingsSearchableItem> {
    val sections = buildSectionRequests(strings.settings)
    val items = mutableListOf<SettingsSearchableItem>()

    // Add all main sections
    sections.forEach { request ->
        items.add(
            SettingsSearchableItem.Section(
                id = request.section.name,
                title = request.title,
                keywords = listOf(request.description),
                navigationRequest = request,
            ),
        )
    }

    // Add Account subitems
    items.addAll(
        listOf(
            SettingsSearchableItem.SubItem(
                id = "account_display_name",
                title = strings.account.displayNameLabel,
                parentSection = SettingsSection.Account,
                keywords = listOf("name", "profile", "username", "identity"),
            ),
            SettingsSearchableItem.SubItem(
                id = "account_status",
                title = strings.account.statusLabel,
                parentSection = SettingsSection.Account,
                keywords = listOf("status", "message", "bio", "about", "available", "online status"),
            ),
            SettingsSearchableItem.SubItem(
                id = "account_phone",
                title = strings.account.phoneLabel,
                parentSection = SettingsSection.Account,
                keywords = listOf("phone", "number", "linked", "mobile"),
            ),
            SettingsSearchableItem.SubItem(
                id = "account_email",
                title = strings.account.emailLabel,
                parentSection = SettingsSection.Account,
                keywords = listOf("email", "address", "recovery", "contact"),
            ),
        ),
    )

    // Add Notification subitems
    items.addAll(
        listOf(
            SettingsSearchableItem.SubItem(
                id = "notifications_push",
                title = strings.notifications.pushTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("push", "alert", "banner", "badge", "notification"),
            ),
            SettingsSearchableItem.SubItem(
                id = "notifications_sound",
                title = strings.notifications.soundTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("sound", "tone", "ringtone", "audio", "chime", "popcorn", "ripple"),
            ),
            SettingsSearchableItem.SubItem(
                id = "notifications_quiet_hours",
                title = strings.notifications.quietHoursTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("quiet", "hours", "schedule", "mute", "do not disturb", "dnd", "silent"),
            ),
            SettingsSearchableItem.SubItem(
                id = "notifications_vibration",
                title = strings.notifications.vibrationTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("vibration", "haptic", "feedback"),
            ),
            SettingsSearchableItem.SubItem(
                id = "notifications_preview",
                title = strings.notifications.previewTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("preview", "message", "show", "display", "content"),
            ),
            SettingsSearchableItem.SubItem(
                id = "notifications_reset",
                title = strings.notifications.resetTitle,
                parentSection = SettingsSection.Notifications,
                keywords = listOf("reset", "default", "restore"),
            ),
        ),
    )

    // Add Appearance subitems
    items.addAll(
        listOf(
            SettingsSearchableItem.SubItem(
                id = "appearance_theme_system",
                title = strings.appearance.themeSystemTitle,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("system", "auto", "automatic", "default", "device", "theme"),
            ),
            SettingsSearchableItem.SubItem(
                id = "appearance_theme_light",
                title = strings.appearance.themeLightTitle,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("light", "bright", "day", "theme"),
            ),
            SettingsSearchableItem.SubItem(
                id = "appearance_theme_dark",
                title = strings.appearance.themeDarkTitle,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("dark", "night", "black", "theme"),
            ),
            SettingsSearchableItem.SubItem(
                id = "appearance_text_scale",
                title = strings.appearance.typographySection,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("text", "font", "size", "scale", "typography", "large", "small"),
            ),
            SettingsSearchableItem.SubItem(
                id = "appearance_reduce_motion",
                title = strings.appearance.reduceMotionTitle,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("motion", "animation", "reduce", "minimize", "accessibility"),
            ),
            SettingsSearchableItem.SubItem(
                id = "appearance_high_contrast",
                title = strings.appearance.highContrastTitle,
                parentSection = SettingsSection.Appearance,
                keywords = listOf("contrast", "high", "accessibility", "legibility", "visibility"),
            ),
        ),
    )

    // Add Privacy subitems
    items.addAll(
        listOf(
            SettingsSearchableItem.SubItem(
                id = "privacy_blocked_contacts",
                title = strings.privacy.blockedContactsTitle,
                parentSection = SettingsSection.Privacy,
                keywords = listOf("blocked", "block", "contacts", "users", "ban", "restrict"),
            ),
            SettingsSearchableItem.SubItem(
                id = "privacy_invite_preferences",
                title = strings.privacy.invitePreferencesTitle,
                parentSection = SettingsSection.Privacy,
                keywords = listOf("invite", "group", "preferences", "who can add", "permissions"),
            ),
            SettingsSearchableItem.SubItem(
                id = "privacy_last_seen",
                title = strings.privacy.lastSeenTitle,
                parentSection = SettingsSection.Privacy,
                keywords = listOf("last seen", "online", "status", "visibility", "presence"),
            ),
            SettingsSearchableItem.SubItem(
                id = "privacy_read_receipts",
                title = strings.privacy.readReceiptsTitle,
                parentSection = SettingsSection.Privacy,
                keywords = listOf("read", "receipts", "seen", "checkmarks", "blue ticks"),
            ),
            SettingsSearchableItem.SubItem(
                id = "privacy_usage_data",
                title = strings.privacy.shareUsageDataTitle,
                parentSection = SettingsSection.Privacy,
                keywords = listOf("usage", "data", "analytics", "diagnostic", "telemetry", "share"),
            ),
        ),
    )

    return items
}

private fun getSectionIcon(section: SettingsSection): ImageVector =
    when (section) {
        SettingsSection.Account -> Icons.Rounded.Person
        SettingsSection.Notifications -> Icons.Rounded.Notifications
        SettingsSection.Appearance -> Icons.Rounded.DarkMode
        SettingsSection.Privacy -> Icons.Rounded.Lock
    }

private fun getSectionIconBackground(section: SettingsSection): Color =
    when (section) {
        SettingsSection.Account -> SettingsIconBlue
        SettingsSection.Notifications -> SettingsIconRed
        SettingsSection.Appearance -> SettingsIconPurple
        SettingsSection.Privacy -> SettingsIconGreen
    }

private val SettingsIconBlue = Color(0xFF007AFF)
private val SettingsIconRed = Color(0xFFFF3B30)
private val SettingsIconPurple = Color(0xFF5856D6)
private val SettingsIconGreen = Color(0xFF34C759)
private val SettingsIconGray = Color(0xFF8E8E93)

@DevicePreviews
@Preview
@Composable
private fun SettingsScreenPreview() {
    LiveChatPreviewContainer {
        SettingsScreen()
    }
}
