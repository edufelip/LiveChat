package com.edufelip.livechat.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.edufelip.livechat.resources.Res
import org.jetbrains.compose.resources.stringResource

@Suppress("LongParameterList")
data class ContactsStrings(
    val loading: String = "Loading contacts…",
    val emptyState: String = "No contacts synced yet",
    val syncCta: String = "Sync Contacts",
    val syncing: String = "Syncing…",
    val syncingStateDescription: String = "Syncing contacts",
    val onLiveChatBadge: String = "On LiveChat",
    val inviteCta: String = "Invite",
    val inviteShareTitle: String = "Share LiveChat invite",
    val inviteShareUnavailable: String = "No apps available to share",
    val permissionDeniedMessage: String = "Enable contacts permission to sync your phonebook.",
    val registeredSectionTitle: String = "On LiveChat",
    val inviteSectionTitle: String = "Invite to LiveChat",
    val validatingSectionMessage: String = "Validating contacts...",
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
    val openingSectionTemplate: String = "Opening %1\$s settings soon",
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
    val resendCountdownLabel: @Composable (Int) -> String = { seconds ->
        "Resend available in ${seconds.coerceAtLeast(0)}s"
    },
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

@Composable
fun rememberLiveChatStrings(): LiveChatStrings {
    val contacts =
        ContactsStrings(
            loading = stringResource(Res.string.contacts_loading),
            emptyState = stringResource(Res.string.contacts_empty_state),
            syncCta = stringResource(Res.string.contacts_sync_cta),
            syncing = stringResource(Res.string.contacts_syncing),
            syncingStateDescription = stringResource(Res.string.contacts_syncing_state_description),
            onLiveChatBadge = stringResource(Res.string.contacts_on_livechat_badge),
            inviteCta = stringResource(Res.string.contacts_invite_cta),
            inviteShareTitle = stringResource(Res.string.contacts_invite_share_title),
            inviteShareUnavailable = stringResource(Res.string.contacts_invite_share_unavailable),
            permissionDeniedMessage = stringResource(Res.string.contacts_permission_denied_message),
            registeredSectionTitle = stringResource(Res.string.contacts_registered_section_title),
            inviteSectionTitle = stringResource(Res.string.contacts_invite_section_title),
            validatingSectionMessage = stringResource(Res.string.contacts_validating_section_label),
        )

    val settings =
        SettingsStrings(
            screenTitle = stringResource(Res.string.settings_screen_title),
            accountTitle = stringResource(Res.string.settings_account_title),
            accountDescription = stringResource(Res.string.settings_account_description),
            notificationsTitle = stringResource(Res.string.settings_notifications_title),
            notificationsDescription = stringResource(Res.string.settings_notifications_description),
            appearanceTitle = stringResource(Res.string.settings_appearance_title),
            appearanceDescription = stringResource(Res.string.settings_appearance_description),
            privacyTitle = stringResource(Res.string.settings_privacy_title),
            privacyDescription = stringResource(Res.string.settings_privacy_description),
            openingSectionTemplate = stringResource(Res.string.settings_opening_section_toast),
        )

    val home =
        HomeStrings(
            conversationTitle = stringResource(Res.string.home_conversation_title),
            chatsTab = stringResource(Res.string.home_chats_tab),
            contactsTab = stringResource(Res.string.home_contacts_tab),
            settingsTab = stringResource(Res.string.home_settings_tab),
            backCta = stringResource(Res.string.home_back_cta),
        )

    val onboarding =
        OnboardingStrings(
            phoneTitle = stringResource(Res.string.onboarding_phone_title),
            phoneSubtitle = stringResource(Res.string.onboarding_phone_subtitle),
            phoneFieldLabel = stringResource(Res.string.onboarding_phone_field_label),
            phoneFieldPlaceholder = stringResource(Res.string.onboarding_phone_field_placeholder),
            continueCta = stringResource(Res.string.onboarding_continue_cta),
            defaultPhoneSample = stringResource(Res.string.onboarding_default_phone_sample),
            invalidPhoneError = stringResource(Res.string.onboarding_invalid_phone_error),
            startVerificationError = stringResource(Res.string.onboarding_start_verification_error),
            otpTitle = stringResource(Res.string.onboarding_otp_title),
            otpSubtitle = stringResource(Res.string.onboarding_otp_subtitle),
            otpFieldLabel = stringResource(Res.string.onboarding_otp_field_label),
            resendCta = stringResource(Res.string.onboarding_resend_cta),
            resendCountdownLabel = { seconds ->
                stringResource(
                    Res.string.onboarding_resend_countdown_label,
                    seconds.coerceAtLeast(0),
                )
            },
            verifyCta = stringResource(Res.string.onboarding_verify_cta),
            successTitle = stringResource(Res.string.onboarding_success_title),
            successSubtitle = stringResource(Res.string.onboarding_success_subtitle),
            successCta = stringResource(Res.string.onboarding_success_cta),
            countryPickerTitle = stringResource(Res.string.onboarding_country_picker_title),
            countryPickerSearchPlaceholder = stringResource(Res.string.onboarding_country_picker_search_placeholder),
            countryPickerEmpty = stringResource(Res.string.onboarding_country_picker_empty),
            countryPickerClose = stringResource(Res.string.onboarding_country_picker_close),
            invalidVerificationCode = stringResource(Res.string.onboarding_invalid_verification_code),
            tooManyRequests = stringResource(Res.string.onboarding_too_many_requests),
            quotaExceeded = stringResource(Res.string.onboarding_quota_exceeded),
            codeExpired = stringResource(Res.string.onboarding_code_expired),
            networkError = stringResource(Res.string.onboarding_network_error),
            resendNotAvailable = stringResource(Res.string.onboarding_resend_not_available),
            configurationError = stringResource(Res.string.onboarding_configuration_error),
            unknownError = stringResource(Res.string.onboarding_unknown_error),
        )

    val general =
        GeneralStrings(
            ok = stringResource(Res.string.general_ok),
            errorTitle = stringResource(Res.string.general_error_title),
        )

    return LiveChatStrings(
        contacts = contacts,
        settings = settings,
        home = home,
        onboarding = onboarding,
        general = general,
    )
}
