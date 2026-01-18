package com.edufelip.livechat.ui.features.settings.account

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.phoneAuthPresentationContext
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.models.EmailUpdateState
import com.edufelip.livechat.domain.utils.phoneNumberFromE164
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.features.settings.account.components.AccountDeleteBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.AccountEditBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.AccountEmailBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.DeleteBottomSheetStep
import com.edufelip.livechat.ui.features.settings.account.components.EmailBottomSheetStep
import com.edufelip.livechat.ui.platform.rememberPlatformContext
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.OnboardingStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAccountPresenter
import com.edufelip.livechat.ui.state.rememberPhoneAuthPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBack,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        AccountSettingsScreen(
            modifier = modifier,
            state = previewState(),
            onBack = onBack,
            onEditDisplayName = {},
            onEditStatus = {},
            onEditEmail = {},
            onDeleteAccount = {},
        )
        return
    }

    val presenter = rememberAccountPresenter()
    val state by presenter.collectState()
    val sessionProvider = rememberSessionProvider()
    val phoneAuthPresenter = rememberPhoneAuthPresenter()
    val phoneAuthState by phoneAuthPresenter.collectState()
    val platformContext = rememberPlatformContext()
    val onboardingStrings = strings.onboarding

    var activeEdit by remember { mutableStateOf(EditField.None) }
    var editValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var emailStep by remember { mutableStateOf(EmailBottomSheetStep.Entry) }
    var deleteStep by remember { mutableStateOf<DeleteBottomSheetStep?>(null) }
    var deleteCountdown by remember { mutableStateOf(DELETE_COUNTDOWN_SECONDS) }
    var otpCode by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var emailResendCountdown by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    LaunchedEffect(emailResendCountdown) {
        if (emailResendCountdown > 0) {
            delay(1_000)
            emailResendCountdown -= 1
        }
    }

    LaunchedEffect(activeEdit, state.profile) {
        when (activeEdit) {
            EditField.DisplayName -> editValue = state.profile?.displayName.orEmpty()
            EditField.StatusMessage -> editValue = state.profile?.statusMessage.orEmpty()
            EditField.Email -> emailValue = state.profile?.email.orEmpty()
            EditField.None -> Unit
        }
    }

    LaunchedEffect(activeEdit) {
        if (activeEdit == EditField.Email) {
            emailStep = EmailBottomSheetStep.Entry
            presenter.clearEmailUpdateState()
        } else if (activeEdit == EditField.None) {
            presenter.clearEmailUpdateState()
        }
    }

    LaunchedEffect(state.emailUpdateState) {
        when (val emailState = state.emailUpdateState) {
            is EmailUpdateState.Sent -> {
                emailStep = EmailBottomSheetStep.AwaitVerification
                emailValue = emailState.email
            }
            is EmailUpdateState.Verified -> {
                activeEdit = EditField.None
                presenter.clearEmailUpdateState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            deleteStep = null
            signOutPlatformUser()
            sessionProvider.setSession(null)
            onAccountDeleted()
            presenter.acknowledgeDeletion()
        }
    }

    val sessionPhone = sessionProvider.currentUserPhone()
    val reauthPhone =
        remember(state.profile?.phoneNumber, sessionPhone) {
            val candidate = state.profile?.phoneNumber ?: sessionPhone
            candidate?.let { phoneNumberFromE164(it) }
        }
    val phoneAuthContext =
        remember(platformContext) {
            runCatching { phoneAuthPresentationContext(platformContext) }.getOrNull()
        }

    LaunchedEffect(deleteStep) {
        if (deleteStep == DeleteBottomSheetStep.Countdown) {
            deleteCountdown = DELETE_COUNTDOWN_SECONDS
            while (deleteCountdown > 0) {
                delay(1_000)
                deleteCountdown -= 1
            }
        } else {
            deleteCountdown = DELETE_COUNTDOWN_SECONDS
        }

        if (deleteStep != DeleteBottomSheetStep.Reauth) {
            otpCode = ""
            reauthError = null
        }
    }

    LaunchedEffect(state.requiresReauth) {
        if (state.requiresReauth) {
            deleteStep = DeleteBottomSheetStep.Reauth
        }
    }

    LaunchedEffect(deleteStep, phoneAuthContext, reauthPhone) {
        if (deleteStep == DeleteBottomSheetStep.Reauth) {
            otpCode = ""
            reauthError = null
            when {
                phoneAuthContext == null -> reauthError = onboardingStrings.startVerificationError
                reauthPhone == null -> reauthError = onboardingStrings.invalidPhoneError
                else -> phoneAuthPresenter.startVerification(reauthPhone, phoneAuthContext)
            }
        }
    }

    LaunchedEffect(phoneAuthState.isVerificationCompleted) {
        if (phoneAuthState.isVerificationCompleted && deleteStep == DeleteBottomSheetStep.Reauth) {
            presenter.clearReauthRequirement()
            presenter.requestDeleteAccount()
        }
    }

    val errorMessage = state.errorMessage
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                presenter.clearError()
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    presenter.clearError()
                }) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(strings.general.errorTitle) },
            text = { Text(errorMessage) },
        )
    }

    LaunchedEffect(state.isUpdating, state.errorMessage, activeEdit) {
        val shouldCloseEdit =
            !state.isUpdating &&
                state.errorMessage == null &&
                (activeEdit == EditField.DisplayName || activeEdit == EditField.StatusMessage)
        if (shouldCloseEdit) {
            activeEdit = EditField.None
        }
    }

    if (activeEdit == EditField.DisplayName || activeEdit == EditField.StatusMessage) {
        AccountEditBottomSheet(
            title = activeEdit.title(strings),
            description = activeEdit.description(strings),
            label = activeEdit.label(strings),
            placeholder = activeEdit.placeholder(strings),
            value = editValue,
            onValueChange = { editValue = it },
            onDismiss = { activeEdit = EditField.None },
            onConfirm = {
                when (activeEdit) {
                    EditField.DisplayName -> presenter.updateDisplayName(editValue)
                    EditField.StatusMessage -> presenter.updateStatusMessage(editValue)
                    EditField.Email,
                    EditField.None,
                    -> Unit
                }
            },
            confirmEnabled = activeEdit.canSave(editValue),
            isUpdating = state.isUpdating,
            confirmLabel = strings.account.saveCta,
            keyboardOptions = activeEdit.keyboardOptions(),
        )
    }

    if (activeEdit == EditField.Email) {
        val verifyDescription =
            formatTemplate(strings.account.editEmailVerifyDescription, emailValue)
        AccountEmailBottomSheet(
            step = emailStep,
            title = strings.account.editEmailTitle,
            description = strings.account.editEmailDescription,
            verifyTitle = strings.account.editEmailVerifyTitle,
            verifyDescription = verifyDescription,
            email = emailValue,
            placeholder = strings.account.emailLabel,
            sendLabel = strings.account.editEmailSendCta,
            verifyLabel = strings.account.editEmailVerifyCta,
            changeLabel = strings.account.editEmailChangeCta,
            resendLabel = strings.account.editEmailResendCta,
            resendCountdownLabel = strings.account.editEmailResendCountdownLabel,
            onEmailChange = { emailValue = it },
            onSendVerification = {
                presenter.sendEmailVerification(emailValue)
                emailResendCountdown = EMAIL_RESEND_DELAY_SECONDS
            },
            onConfirmVerified = { presenter.confirmEmailUpdate(emailValue) },
            onChangeEmail = {
                emailStep = EmailBottomSheetStep.Entry
                presenter.clearEmailUpdateState()
            },
            onResend = {
                if (emailResendCountdown <= 0) {
                    presenter.sendEmailVerification(emailValue)
                    emailResendCountdown = EMAIL_RESEND_DELAY_SECONDS
                }
            },
            onDismiss = {
                activeEdit = EditField.None
                presenter.clearEmailUpdateState()
            },
            isLoading = state.isUpdating,
            confirmEnabled = emailValue.trim().isNotEmpty(),
            keyboardOptions = EditField.Email.keyboardOptions(),
            resendCountdown = emailResendCountdown,
        )
    }

    if (deleteStep != null) {
        val phoneLabel = state.profile?.phoneNumber ?: sessionPhone.orEmpty()
        val reauthBody = formatTemplate(strings.account.deleteReauthBody, phoneLabel)
        val otpError =
            reauthError
                ?: phoneAuthState.error?.toMessage(onboardingStrings)
        val countdownCta = formatCountdown(strings.account.deleteCountdownCta, deleteCountdown)
        AccountDeleteBottomSheet(
            step = deleteStep ?: DeleteBottomSheetStep.Confirm,
            confirmTitle = strings.account.deleteConfirmTitle,
            confirmBody = strings.account.deleteConfirmBody,
            confirmCta = strings.account.deleteConfirmCta,
            cancelLabel = strings.general.cancel,
            farewellTitle = strings.account.deleteFarewellTitle,
            farewellBody = strings.account.deleteFarewellBody,
            countdownCta = countdownCta,
            countdownReadyCta = strings.account.deleteCountdownReadyCta,
            countdownSeconds = deleteCountdown,
            reauthTitle = strings.account.deleteReauthTitle,
            reauthBody = reauthBody,
            reauthCodeLabel = strings.account.deleteReauthCodeLabel,
            reauthCodePlaceholder = strings.account.deleteReauthCodePlaceholder,
            reauthCta = strings.account.deleteReauthCta,
            reauthResendLabel = strings.account.deleteReauthResend,
            reauthError = otpError,
            otp = otpCode,
            onOtpChange = { value ->
                if (value.length <= 6 && value.all(Char::isDigit)) {
                    otpCode = value
                    phoneAuthPresenter.dismissError()
                }
            },
            canResend = phoneAuthState.canResend,
            onResend = {
                if (phoneAuthContext != null) {
                    phoneAuthPresenter.resendCode(phoneAuthContext)
                }
            },
            onConfirmDelete = {
                deleteStep = DeleteBottomSheetStep.Countdown
            },
            onConfirmCountdown = {
                presenter.requestDeleteAccount()
            },
            onConfirmReauth = {
                if (otpCode.length == 6) {
                    phoneAuthPresenter.verifyCode(otpCode)
                }
            },
            onCancel = {
                deleteStep = null
                presenter.clearReauthRequirement()
            },
            onDismiss = {
                deleteStep = null
                presenter.clearReauthRequirement()
            },
            isDeleting = state.isDeleting,
            isVerifying = phoneAuthState.isVerifying,
        )
    }

    val allowEdits = !state.isLoading && !state.isUpdating && !state.isDeleting

    // Enable back gesture support
    SettingsSubmenuBackHandler(
        enabled = true,
        onBack = onBack,
    )

    AccountSettingsScreen(
        modifier = modifier,
        state = state,
        phoneNumberOverride = sessionPhone,
        onBack = onBack,
        onEditDisplayName = { if (allowEdits) activeEdit = EditField.DisplayName },
        onEditStatus = { if (allowEdits) activeEdit = EditField.StatusMessage },
        onEditEmail = { if (allowEdits) activeEdit = EditField.Email },
        onDeleteAccount = {
            if (!state.isDeleting) {
                deleteStep = DeleteBottomSheetStep.Confirm
            }
        },
    )
}

private enum class EditField {
    DisplayName,
    StatusMessage,
    Email,
    None,
}

private fun EditField.title(strings: LiveChatStrings): String =
    when (this) {
        EditField.DisplayName -> strings.account.editDisplayNameTitle
        EditField.StatusMessage -> strings.account.editStatusTitle
        EditField.Email -> strings.account.editEmailTitle
        EditField.None -> ""
    }

private fun EditField.description(strings: LiveChatStrings): String =
    when (this) {
        EditField.DisplayName -> strings.account.editDisplayNameDescription
        EditField.StatusMessage -> strings.account.editStatusDescription
        EditField.Email -> strings.account.editEmailDescription
        EditField.None -> ""
    }

private fun EditField.label(strings: LiveChatStrings): String =
    when (this) {
        EditField.DisplayName -> strings.account.displayNameLabel
        EditField.StatusMessage -> strings.account.statusLabel
        EditField.Email -> strings.account.emailLabel
        EditField.None -> ""
    }

private fun EditField.placeholder(strings: LiveChatStrings): String =
    when (this) {
        EditField.DisplayName -> strings.account.displayNameMissing
        EditField.StatusMessage -> strings.account.statusPlaceholder
        EditField.Email -> strings.account.emailMissing
        EditField.None -> ""
    }

private fun EditField.keyboardOptions(): KeyboardOptions =
    when (this) {
        EditField.Email ->
            KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            )
        EditField.DisplayName,
        EditField.StatusMessage,
        EditField.None,
        ->
            KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            )
    }

private fun EditField.canSave(value: String): Boolean =
    when (this) {
        EditField.DisplayName, EditField.Email -> value.trim().isNotEmpty()
        EditField.StatusMessage, EditField.None -> true
    }

private fun formatTemplate(
    template: String,
    value: String,
): String = template.replace("%1\$s", value).replace("%s", value)

private fun formatCountdown(
    template: String,
    seconds: Int,
): String {
    val value = seconds.toString()
    return template.replace("%1\$d", value).replace("%d", value)
}

private fun PhoneAuthError.toMessage(strings: OnboardingStrings): String =
    when (this) {
        PhoneAuthError.InvalidPhoneNumber -> strings.invalidPhoneError
        PhoneAuthError.InvalidVerificationCode -> strings.invalidVerificationCode
        PhoneAuthError.TooManyRequests -> strings.tooManyRequests
        PhoneAuthError.QuotaExceeded -> strings.quotaExceeded
        PhoneAuthError.CodeExpired -> strings.codeExpired
        PhoneAuthError.NetworkError -> strings.networkError
        PhoneAuthError.ResendNotAvailable -> strings.resendNotAvailable
        is PhoneAuthError.Configuration -> this.message ?: strings.configurationError
        is PhoneAuthError.Unknown -> this.message ?: strings.unknownError
    }

private fun previewState(): AccountUiState =
    AccountUiState(
        isLoading = false,
        profile = null,
    )

private const val DELETE_COUNTDOWN_SECONDS = 15
private const val EMAIL_RESEND_DELAY_SECONDS = 300 // 5 minutes

@DevicePreviews
@Preview
@Composable
private fun AccountSettingsRoutePreview() {
    LiveChatPreviewContainer {
        AccountSettingsRoute()
    }
}
