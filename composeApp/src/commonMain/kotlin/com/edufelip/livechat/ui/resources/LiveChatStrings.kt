@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.edufelip.livechat.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.edufelip.livechat.resources.*
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

data class AccountStrings(
    val screenTitle: String = "Account Settings",
    val screenSubtitle: String = "Manage your profile details",
    val editCta: String = "Edit",
    val displayNameLabel: String = "Display Name",
    val displayNameMissing: String = "Add your name",
    val statusLabel: String = "Status Message",
    val phoneLabel: String = "Linked Phone Number",
    val emailLabel: String = "Email Address",
    val phoneReadOnlyHint: String = "Contact support to change",
    val phoneMissing: String = "No phone linked",
    val emailMissing: String = "Add an email",
    val statusPlaceholder: String = "Available for chat",
    val onlineLabel: String = "Online",
    val deleteTitle: String = "Delete Account",
    val deleteDescription: String = "Permanently remove your data",
    val deleteConfirmTitle: String = "Delete account?",
    val deleteConfirmBody: String = "This will permanently remove your data. This action can't be undone.",
    val deleteConfirmCta: String = "Delete",
    val editDisplayNameTitle: String = "Edit display name",
    val editDisplayNameDescription: String = "This is how your name appears to contacts.",
    val editStatusTitle: String = "Edit status message",
    val editStatusDescription: String = "Share a short status with your contacts.",
    val editEmailTitle: String = "Edit email address",
    val editEmailDescription: String = "Use an email for account recovery.",
    val saveCta: String = "Save",
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

data class ConversationStrings(
    val archivedLabel: String = "Archived conversation",
    val mutedLabel: String = "Muted conversation",
    val mutedUntilPrefix: String = "Muted until",
    val loadingMessages: String = "Loading messages…",
    val searchPlaceholder: String = "Search conversations",
    val loadingList: String = "Loading conversations…",
    val emptyList: String = "No conversations yet",
    val pinnedSectionTitle: String = "Pinned",
    val othersSectionTitle: String = "Others",
    val filterAll: String = "All",
    val filterUnread: String = "Unread",
    val filterPinned: String = "Pinned",
    val filterArchived: String = "Archived",
    val audioMessageLabel: String = "Audio message",
    val audioShortLabel: String = "Audio",
    val pinAction: String = "Pin",
    val unpinAction: String = "Unpin",
    val muteAction: String = "Mute",
    val unmuteAction: String = "Unmute",
    val archiveAction: String = "Archive",
    val unarchiveAction: String = "Unarchive",
    val permissionTitle: String = "Permission needed",
    val microphonePermissionHint: String = "Microphone permission is required to record audio.",
    val microphonePermissionDialog: String = "Microphone permission is blocked. Please enable it in Settings.",
    val photoPermissionHint: String = "Allow photo/gallery access to attach images.",
    val photoPermissionDialog: String = "Photo permissions are blocked. Please enable them in Settings.",
    val cameraPermissionHint: String = "Camera permission is required to take a photo.",
    val cameraPermissionDialog: String = "Camera permission is blocked. Please enable it in Settings.",
    val recordingStartError: String = "Unable to start recording.",
    val imageAttachError: String = "Unable to attach image.",
    val photoCaptureError: String = "Unable to capture photo.",
    val recordingCancelDescription: String = "Cancel recording",
    val recordingSendDescription: String = "Send recording",
    val recordingLabel: @Composable (String) -> String = { duration -> "Recording $duration" },
    val playAudioDescription: String = "Play audio",
    val stopAudioDescription: String = "Stop audio",
    val playingAudioLabel: String = "Playing audio",
    val imageMessageDescription: String = "Image message",
    val imageLabel: String = "Image",
    val imageFallbackLabel: @Composable (String) -> String = { name -> "Image: $name" },
    val messagePlaceholder: String = "Message…",
    val messageFailed: String = "Message failed",
    val copyAction: String = "Copy",
    val deleteAction: String = "Delete",
    val messageCopied: String = "Copied to clipboard",
    val retryMessageTitle: String = "Message failed",
    val retryMessageBody: String = "Couldn't send this message. Retry?",
    val retryMessageCta: String = "Retry",
    val sendMessage: String = "Send message",
    val pickImage: String = "Pick image",
    val takePhoto: String = "Take photo",
    val recordAudio: String = "Record audio",
    val stopRecording: String = "Stop recording",
    val sendRecording: String = "Send recording",
)

data class GeneralStrings(
    val ok: String = "OK",
    val cancel: String = "Cancel",
    val openSettings: String = "Open Settings",
    val dismiss: String = "Dismiss",
    val errorTitle: String = "Something went wrong",
)

data class LiveChatStrings(
    val contacts: ContactsStrings = ContactsStrings(),
    val settings: SettingsStrings = SettingsStrings(),
    val account: AccountStrings = AccountStrings(),
    val home: HomeStrings = HomeStrings(),
    val onboarding: OnboardingStrings = OnboardingStrings(),
    val conversation: ConversationStrings = ConversationStrings(),
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

    val account =
        AccountStrings(
            screenTitle = stringResource(Res.string.account_screen_title),
            screenSubtitle = stringResource(Res.string.account_screen_subtitle),
            editCta = stringResource(Res.string.account_edit_cta),
            displayNameLabel = stringResource(Res.string.account_display_name_label),
            displayNameMissing = stringResource(Res.string.account_display_name_missing),
            statusLabel = stringResource(Res.string.account_status_label),
            phoneLabel = stringResource(Res.string.account_phone_label),
            emailLabel = stringResource(Res.string.account_email_label),
            phoneReadOnlyHint = stringResource(Res.string.account_phone_readonly_hint),
            phoneMissing = stringResource(Res.string.account_phone_missing),
            emailMissing = stringResource(Res.string.account_email_missing),
            statusPlaceholder = stringResource(Res.string.account_status_placeholder),
            onlineLabel = stringResource(Res.string.account_online_label),
            deleteTitle = stringResource(Res.string.account_delete_title),
            deleteDescription = stringResource(Res.string.account_delete_description),
            deleteConfirmTitle = stringResource(Res.string.account_delete_confirm_title),
            deleteConfirmBody = stringResource(Res.string.account_delete_confirm_body),
            deleteConfirmCta = stringResource(Res.string.account_delete_confirm_cta),
            editDisplayNameTitle = stringResource(Res.string.account_edit_display_name_title),
            editDisplayNameDescription = stringResource(Res.string.account_edit_display_name_description),
            editStatusTitle = stringResource(Res.string.account_edit_status_title),
            editStatusDescription = stringResource(Res.string.account_edit_status_description),
            editEmailTitle = stringResource(Res.string.account_edit_email_title),
            editEmailDescription = stringResource(Res.string.account_edit_email_description),
            saveCta = stringResource(Res.string.account_save_cta),
        )

    val home =
        HomeStrings(
            conversationTitle = stringResource(Res.string.home_conversation_title),
            chatsTab = stringResource(Res.string.home_chats_tab),
            contactsTab = stringResource(Res.string.home_contacts_tab),
            settingsTab = stringResource(Res.string.home_settings_tab),
            backCta = stringResource(Res.string.home_back_cta),
        )

    val conversation =
        ConversationStrings(
            archivedLabel = stringResource(Res.string.conversation_archived_label),
            mutedLabel = stringResource(Res.string.conversation_muted_label),
            mutedUntilPrefix = stringResource(Res.string.conversation_muted_until_prefix),
            loadingMessages = stringResource(Res.string.conversation_loading_messages),
            searchPlaceholder = stringResource(Res.string.conversation_search_placeholder),
            loadingList = stringResource(Res.string.conversation_loading_list),
            emptyList = stringResource(Res.string.conversation_empty_list),
            pinnedSectionTitle = stringResource(Res.string.conversation_section_pinned),
            othersSectionTitle = stringResource(Res.string.conversation_section_others),
            filterAll = stringResource(Res.string.conversation_filter_all),
            filterUnread = stringResource(Res.string.conversation_filter_unread),
            filterPinned = stringResource(Res.string.conversation_filter_pinned),
            filterArchived = stringResource(Res.string.conversation_filter_archived),
            audioMessageLabel = stringResource(Res.string.conversation_audio_message_label),
            audioShortLabel = stringResource(Res.string.conversation_audio_short_label),
            pinAction = stringResource(Res.string.conversation_pin_action),
            unpinAction = stringResource(Res.string.conversation_unpin_action),
            muteAction = stringResource(Res.string.conversation_mute_action),
            unmuteAction = stringResource(Res.string.conversation_unmute_action),
            archiveAction = stringResource(Res.string.conversation_archive_action),
            unarchiveAction = stringResource(Res.string.conversation_unarchive_action),
            permissionTitle = stringResource(Res.string.conversation_permission_title),
            microphonePermissionHint = stringResource(Res.string.conversation_microphone_permission_hint),
            microphonePermissionDialog = stringResource(Res.string.conversation_microphone_permission_dialog),
            photoPermissionHint = stringResource(Res.string.conversation_photo_permission_hint),
            photoPermissionDialog = stringResource(Res.string.conversation_photo_permission_dialog),
            cameraPermissionHint = stringResource(Res.string.conversation_camera_permission_hint),
            cameraPermissionDialog = stringResource(Res.string.conversation_camera_permission_dialog),
            recordingStartError = stringResource(Res.string.conversation_recording_start_error),
            imageAttachError = stringResource(Res.string.conversation_image_attach_error),
            photoCaptureError = stringResource(Res.string.conversation_photo_capture_error),
            recordingCancelDescription = stringResource(Res.string.conversation_recording_cancel_description),
            recordingSendDescription = stringResource(Res.string.conversation_recording_send_description),
            recordingLabel = { duration ->
                stringResource(
                    Res.string.conversation_recording_label,
                    duration,
                )
            },
            playAudioDescription = stringResource(Res.string.conversation_play_audio_description),
            stopAudioDescription = stringResource(Res.string.conversation_stop_audio_description),
            playingAudioLabel = stringResource(Res.string.conversation_playing_audio_label),
            imageMessageDescription = stringResource(Res.string.conversation_image_message_description),
            imageLabel = stringResource(Res.string.conversation_image_label),
            imageFallbackLabel = { name ->
                stringResource(
                    Res.string.conversation_image_fallback_template,
                    name,
                )
            },
            messagePlaceholder = stringResource(Res.string.conversation_message_placeholder),
            messageFailed = stringResource(Res.string.conversation_message_failed),
            copyAction = stringResource(Res.string.conversation_message_copy_action),
            deleteAction = stringResource(Res.string.conversation_message_delete_action),
            messageCopied = stringResource(Res.string.conversation_message_copied),
            retryMessageTitle = stringResource(Res.string.conversation_message_retry_title),
            retryMessageBody = stringResource(Res.string.conversation_message_retry_body),
            retryMessageCta = stringResource(Res.string.conversation_message_retry_cta),
            sendMessage = stringResource(Res.string.conversation_send_message),
            pickImage = stringResource(Res.string.conversation_pick_image),
            takePhoto = stringResource(Res.string.conversation_take_photo),
            recordAudio = stringResource(Res.string.conversation_record_audio),
            stopRecording = stringResource(Res.string.conversation_stop_recording),
            sendRecording = stringResource(Res.string.conversation_send_recording),
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
            cancel = stringResource(Res.string.general_cancel),
            openSettings = stringResource(Res.string.general_open_settings),
            dismiss = stringResource(Res.string.general_dismiss),
            errorTitle = stringResource(Res.string.general_error_title),
        )

    return LiveChatStrings(
        contacts = contacts,
        settings = settings,
        account = account,
        home = home,
        conversation = conversation,
        onboarding = onboarding,
        general = general,
    )
}
