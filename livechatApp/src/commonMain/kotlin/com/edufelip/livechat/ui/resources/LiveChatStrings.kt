package com.edufelip.livechat.ui.resources

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

data class OnboardingStrings(
    val phoneTitle: String = "Enter your phone number",
    val phoneSubtitle: String = "We'll send a verification code to confirm it's you.",
    val phoneFieldLabel: String = "Phone number",
    val phoneFieldPlaceholder: String = "Digits only",
    val continueCta: String = "Continue",
    val defaultPhoneSample: String = "5550100",
    val invalidPhoneError: String = "Please enter a valid phone number",
    val startVerificationError: String = "Unable to start verification",
    val otpTitle: String = "Enter the 6-digit code",
    val otpSubtitle: String = "We just sent a verification code to your number.",
    val otpFieldLabel: String = "Code",
    val resendCta: String = "Resend code",
    val resendCountdownLabel: (Int) -> String = { seconds -> "Resend available in ${seconds.coerceAtLeast(0)}s" },
    val verifyCta: String = "Verify",
    val successTitle: String = "You're all set!",
    val successSubtitle: String = "Your account is ready. Start chatting with your contacts now.",
    val successCta: String = "Start chatting",
    val countryPickerTitle: String = "Select your country",
    val countryPickerSearchPlaceholder: String = "Search by country or code",
    val countryPickerEmpty: String = "No countries found",
    val countryPickerClose: String = "Close",
    val invalidVerificationCode: String = "Invalid verification code",
    val tooManyRequests: String = "Too many attempts. Try again later.",
    val quotaExceeded: String = "SMS quota exceeded. Please try again later.",
    val codeExpired: String = "The code has expired. Request a new one.",
    val networkError: String = "Network error. Check your connection.",
    val resendNotAvailable: String = "You can request a new code shortly.",
    val configurationError: String = "Phone authentication is not configured.",
    val unknownError: String = "Unexpected error. Try again.",
)

data class GeneralStrings(
    val ok: String = "OK",
    val errorTitle: String = "Something went wrong",
)

data class LiveChatStrings(
    val contacts: ContactsStrings = ContactsStrings(),
    val settings: SettingsStrings = SettingsStrings(),
    val home: HomeStrings = HomeStrings(),
    val onboarding: OnboardingStrings = OnboardingStrings(),
    val general: GeneralStrings = GeneralStrings(),
)

val LocalLiveChatStrings = staticCompositionLocalOf { LiveChatStrings() }

@Composable
fun liveChatStrings(): LiveChatStrings = LocalLiveChatStrings.current
