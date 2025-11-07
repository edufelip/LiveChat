package com.project.livechat.composeapp.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

@Suppress("LongParameterList")
data class ContactsStrings(
    val loading: String = "Loading contacts…",
    val emptyState: String = "No contacts synced yet",
    val syncCta: String = "Sync Contacts",
    val syncing: String = "Syncing…",
    val syncingStateDescription: String = "Syncing contacts",
    val onLiveChatBadge: String = "On LiveChat",
    val invitedBadge: String = "Invited",
    val inviteCta: String = "Invite",
    val inviteHistoryTitle: String = "Invite history",
    val inviteDialogTitle: String = "Invite via",
    val inviteDialogCancel: String = "Cancel",
    val permissionDeniedMessage: String = "Enable contacts permission to sync your phonebook.",
)

data class SettingsStrings(
    val screenTitle: String = "Make LiveChat yours",
    val accountTitle: String = "Account",
    val accountDescription: String = "Profile, status, and linked phone number",
    val notificationsTitle: String = "Notifications",
    val notificationsDescription: String = "Mute, schedule quiet hours, sounds",
    val appearanceTitle: String = "Appearance",
    val appearanceDescription: String = "Themes, typography scale, accessibility",
    val privacyTitle: String = "Privacy",
    val privacyDescription: String = "Blocked contacts, invite preferences",
)

data class HomeStrings(
    val conversationTitle: String = "Conversation",
    val chatsTab: String = "Chats",
    val contactsTab: String = "Contacts",
    val settingsTab: String = "Settings",
    val backCta: String = "Back",
)

data class GeneralStrings(
    val ok: String = "OK",
    val errorTitle: String = "Something went wrong",
)

data class LiveChatStrings(
    val contacts: ContactsStrings = ContactsStrings(),
    val settings: SettingsStrings = SettingsStrings(),
    val home: HomeStrings = HomeStrings(),
    val general: GeneralStrings = GeneralStrings(),
)

val LocalLiveChatStrings = staticCompositionLocalOf { LiveChatStrings() }

@Composable
fun liveChatStrings(): LiveChatStrings = LocalLiveChatStrings.current
