package com.edufelip.livechat.ui.features.settings.account

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.phoneAuthPresentationContext
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.utils.phoneNumberFromE164
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.features.conversations.detail.MediaResult
import com.edufelip.livechat.ui.features.conversations.detail.PermissionEvent
import com.edufelip.livechat.ui.features.conversations.detail.rememberConversationMediaController
import com.edufelip.livechat.ui.features.conversations.detail.rememberPermissionViewModel
import com.edufelip.livechat.ui.features.settings.account.components.AccountDeleteBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.AccountEditBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.AccountPhotoBottomSheet
import com.edufelip.livechat.ui.features.settings.account.components.DeleteBottomSheetStep
import com.edufelip.livechat.ui.platform.openAppSettings
import com.edufelip.livechat.ui.platform.rememberPlatformContext
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.OnboardingStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAccountPresenter
import com.edufelip.livechat.ui.state.rememberPhoneAuthPresenter
import com.edufelip.livechat.ui.state.rememberSessionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsRoute(
    modifier: Modifier = Modifier,
    targetItemId: String? = null,
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
            onEditPhoto = {},
            onEditStatus = {},
            onEditEmail = {},
            onDeleteAccount = {},
        )
        return
    }

    val presenter = rememberAccountPresenter()
    val state by presenter.collectState()
    val scope = rememberCoroutineScope()
    val sessionProvider = rememberSessionProvider()
    val phoneAuthPresenter = rememberPhoneAuthPresenter()
    val phoneAuthState by phoneAuthPresenter.collectState()
    val mediaController = rememberConversationMediaController()
    val permissionViewModel = rememberPermissionViewModel()
    val permissionUiState by permissionViewModel.uiState.collectAsState()
    val platformContext = rememberPlatformContext()
    val onboardingStrings = strings.onboarding
    val conversationStrings = strings.conversation

    fun handlePhotoResult(
        result: MediaResult<String>,
        hint: String,
        dialog: String,
        errorFallback: String,
    ) {
        when (result) {
            is MediaResult.Success -> {
                presenter.updatePhoto(result.value)
                permissionViewModel.clearAll()
            }
            is MediaResult.Permission -> {
                permissionViewModel.handlePermission(
                    status = result.status,
                    hint = hint,
                    dialog = dialog,
                )
            }
            MediaResult.Cancelled -> permissionViewModel.clearAll()
            is MediaResult.Error -> permissionViewModel.onError(result.message ?: errorFallback)
        }
    }
    var activeEdit by remember { mutableStateOf(EditField.None) }
    var editValue by remember { mutableStateOf("") }
    var photoSheetRequested by remember { mutableStateOf(false) }
    var deleteStep by remember { mutableStateOf<DeleteBottomSheetStep?>(null) }
    var deleteCountdown by remember { mutableStateOf(DELETE_COUNTDOWN_SECONDS) }
    var otpCode by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var pendingEditSave by remember { mutableStateOf(false) }
    var pendingEditField by remember { mutableStateOf<EditField?>(null) }
    var pendingEditValue by remember { mutableStateOf("") }

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    LaunchedEffect(permissionViewModel) {
        permissionViewModel.events.collect { event ->
            when (event) {
                PermissionEvent.OpenSettings -> openAppSettings()
            }
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

    val permissionDialogMessage = permissionUiState.dialogMessage
    if (permissionDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { permissionViewModel.clearDialog() },
            confirmButton = {
                TextButton(onClick = {
                    permissionViewModel.clearDialog()
                    permissionViewModel.requestOpenSettings()
                }) {
                    Text(strings.general.openSettings)
                }
            },
            dismissButton = {
                TextButton(onClick = { permissionViewModel.clearDialog() }) {
                    Text(strings.general.cancel)
                }
            },
            title = { Text(conversationStrings.permissionTitle) },
            text = { Text(permissionDialogMessage) },
        )
    } else if (permissionUiState.hintMessage != null) {
        AlertDialog(
            onDismissRequest = { permissionViewModel.clearAll() },
            confirmButton = {
                TextButton(onClick = { permissionViewModel.clearAll() }) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(conversationStrings.permissionTitle) },
            text = { Text(permissionUiState.hintMessage.orEmpty()) },
        )
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

    LaunchedEffect(
        pendingEditSave,
        pendingEditField,
        pendingEditValue,
        state.profile?.displayName,
        state.profile?.statusMessage,
        state.isUpdating,
        state.errorMessage,
        activeEdit,
    ) {
        val currentValue =
            when (pendingEditField) {
                EditField.DisplayName -> state.profile?.displayName
                EditField.StatusMessage -> state.profile?.statusMessage
                else -> null
            }
        val shouldCloseEdit =
            pendingEditSave &&
                pendingEditField == activeEdit &&
                !state.isUpdating &&
                state.errorMessage == null &&
                currentValue != null &&
                currentValue.trim() == pendingEditValue
        if (shouldCloseEdit) {
            activeEdit = EditField.None
        }
        if (pendingEditSave && !state.isUpdating) {
            pendingEditSave = false
            pendingEditField = null
            pendingEditValue = ""
        }
    }

    if (activeEdit != EditField.None) {
        AccountEditBottomSheet(
            title = activeEdit.title(strings),
            description = activeEdit.description(strings),
            label = activeEdit.label(strings),
            placeholder = activeEdit.placeholder(strings),
            value = editValue,
            onValueChange = { editValue = it },
            onDismiss = {
                pendingEditSave = false
                pendingEditField = null
                pendingEditValue = ""
                activeEdit = EditField.None
            },
            onConfirm = {
                val trimmedValue = editValue.trim()
                pendingEditSave = true
                pendingEditField = activeEdit
                pendingEditValue = trimmedValue
                when (activeEdit) {
                    EditField.DisplayName -> presenter.updateDisplayName(trimmedValue)
                    EditField.StatusMessage -> presenter.updateStatusMessage(trimmedValue)
                    EditField.Email -> presenter.updateEmail(trimmedValue)
                    EditField.None -> Unit
                }
            },
            confirmEnabled = activeEdit.canSave(editValue),
            isUpdating = state.isUpdating,
            confirmLabel = strings.account.saveCta,
            keyboardOptions = activeEdit.keyboardOptions(),
        )
    }

    if (photoSheetRequested) {
        AccountPhotoBottomSheet(
            title = strings.account.photoSheetTitle,
            description = strings.account.photoSheetDescription,
            pickLabel = conversationStrings.pickImage,
            takeLabel = conversationStrings.takePhoto,
            onPick = {
                photoSheetRequested = false
                scope.launch {
                    val result = mediaController.pickImage()
                    handlePhotoResult(
                        result = result,
                        hint = conversationStrings.photoPermissionHint,
                        dialog = conversationStrings.photoPermissionDialog,
                        errorFallback = conversationStrings.imageAttachError,
                    )
                }
            },
            onTake = {
                photoSheetRequested = false
                scope.launch {
                    val result = mediaController.capturePhoto()
                    handlePhotoResult(
                        result = result,
                        hint = conversationStrings.cameraPermissionHint,
                        dialog = conversationStrings.cameraPermissionDialog,
                        errorFallback = conversationStrings.photoCaptureError,
                    )
                }
            },
            onDismiss = { photoSheetRequested = false },
            isProcessing = state.isUpdating,
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
        targetItemId = targetItemId,
        onBack = onBack,
        onEditDisplayName = {
            if (allowEdits) {
                pendingEditSave = false
                pendingEditField = null
                pendingEditValue = ""
                editValue = state.profile?.displayName.orEmpty()
                activeEdit = EditField.DisplayName
            }
        },
        onEditPhoto = {
            if (allowEdits) {
                photoSheetRequested = true
            }
        },
        onEditStatus = {
            if (allowEdits) {
                pendingEditSave = false
                pendingEditField = null
                pendingEditValue = ""
                editValue = state.profile?.statusMessage.orEmpty()
                activeEdit = EditField.StatusMessage
            }
        },
        onEditEmail = {
            if (allowEdits) {
                pendingEditSave = false
                pendingEditField = null
                pendingEditValue = ""
                editValue = state.profile?.email.orEmpty()
                activeEdit = EditField.Email
            }
        },
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

@DevicePreviews
@Preview
@Composable
private fun AccountSettingsRoutePreview() {
    LiveChatPreviewContainer {
        AccountSettingsRoute()
    }
}
