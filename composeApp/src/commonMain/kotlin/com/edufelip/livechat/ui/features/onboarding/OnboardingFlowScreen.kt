package com.edufelip.livechat.ui.features.onboarding

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.auth.phone.model.phoneAuthPresentationContext
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.dialogs.CountryPickerDialog
import com.edufelip.livechat.ui.features.onboarding.steps.OTPStep
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.features.onboarding.steps.SuccessStep
import com.edufelip.livechat.ui.platform.rememberPlatformContext
import com.edufelip.livechat.ui.resources.OnboardingStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberPhoneAuthPresenter
import com.edufelip.livechat.ui.util.isDigitsOnly
import com.edufelip.livechat.ui.util.isUiTestMode
import com.edufelip.livechat.ui.util.uiTestOverrides
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingFlowScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiTestMode = isUiTestMode()
    if (uiTestMode) {
        UiTestOnboardingFlow(onFinished = onFinished, modifier = modifier)
        return
    }
    val strings = liveChatStrings()
    val phoneAuthPresenter = rememberPhoneAuthPresenter()
    val phoneAuthState by phoneAuthPresenter.collectState()
    var selectedCountryCode by rememberSaveable { mutableStateOf(CountryOption.default().isoCode) }
    val selectedCountry = remember(selectedCountryCode) { CountryOption.fromIsoCode(selectedCountryCode) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneInputError by remember { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current
    val context = rememberPlatformContext()
    val allowVerification = !inspectionMode

    LaunchedEffect(phoneAuthState.session?.verificationId) {
        otp = ""
    }

    val currentStep =
        when {
            phoneAuthState.isVerificationCompleted -> OnboardingStep.Success
            phoneAuthState.session != null -> OnboardingStep.OTP
            else -> OnboardingStep.PhoneEntry
        }

    val phoneErrorMessage =
        phoneInputError
            ?: phoneAuthState.error
                ?.takeIf { phoneAuthState.session == null }
                ?.toMessage(strings.onboarding)
    val otpErrorMessage =
        phoneAuthState.error
            ?.takeIf { phoneAuthState.session != null }
            ?.toMessage(strings.onboarding)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        when (currentStep) {
            OnboardingStep.PhoneEntry ->
                PhoneStep(
                    modifier = contentModifier,
                    selectedCountry = selectedCountry,
                    phoneNumber = phoneNumber,
                    phoneError = phoneErrorMessage,
                    isLoading = phoneAuthState.isRequesting,
                    onPickCountry = { showCountryPicker = true },
                    onPhoneChanged = { input ->
                        phoneNumber = input.filter(Char::isDigit).take(20)
                        phoneInputError = null
                        phoneAuthPresenter.dismissError()
                    },
                    onContinue = {
                        if (!phoneNumber.isDigitsOnly() || phoneNumber.length < 7) {
                            phoneInputError = strings.onboarding.invalidPhoneError
                            return@PhoneStep
                        }
                        if (!allowVerification) return@PhoneStep
                        val presentationContext =
                            runCatching { phoneAuthPresentationContext(context) }
                                .getOrElse {
                                    phoneInputError = strings.onboarding.startVerificationError
                                    return@PhoneStep
                                }
                        phoneAuthPresenter.startVerification(
                            PhoneNumber(
                                dialCode = selectedCountry.dialCode,
                                nationalNumber = phoneNumber,
                            ),
                            presentationContext,
                        )
                    },
                )

            OnboardingStep.OTP ->
                OTPStep(
                    modifier = contentModifier,
                    otp = otp,
                    countdown = phoneAuthState.countdownSeconds,
                    canResend = phoneAuthState.canResend,
                    isRequesting = phoneAuthState.isRequesting,
                    isVerifying = phoneAuthState.isVerifying,
                    errorMessage = otpErrorMessage,
                    onOtpChanged = { value ->
                        if (value.length <= 6 && value.all(Char::isDigit)) {
                            otp = value
                            phoneAuthPresenter.dismissError()
                        }
                    },
                    onResend = {
                        if (allowVerification) {
                            phoneAuthState.session?.let {
                                val presentationContext =
                                    runCatching { phoneAuthPresentationContext(context) }.getOrNull()
                                        ?: return@OTPStep
                                phoneAuthPresenter.resendCode(presentationContext)
                            }
                        }
                    },
                    onVerify = {
                        if (allowVerification && otp.length == 6) {
                            phoneAuthPresenter.verifyCode(otp)
                        }
                    },
                )

            OnboardingStep.Success -> SuccessStep(onFinished = onFinished)
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentSelection = selectedCountry,
            onDismiss = { showCountryPicker = false },
            onSelect = {
                selectedCountryCode = it.isoCode
                showCountryPicker = false
            },
        )
    }
}

@Composable
private fun UiTestOnboardingFlow(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val overrides = uiTestOverrides()
    val phoneOverride = remember(overrides.phone) { overrides.phone?.filter(Char::isDigit) }
    val otpOverride = remember(overrides.otp) { overrides.otp?.filter(Char::isDigit)?.take(6) }
    var selectedCountryCode by rememberSaveable { mutableStateOf(CountryOption.default().isoCode) }
    val selectedCountry = remember(selectedCountryCode) { CountryOption.fromIsoCode(selectedCountryCode) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneInputError by remember { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var step by rememberSaveable { mutableStateOf(OnboardingStep.PhoneEntry) }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.UI_TEST_MODE),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        when (step) {
            OnboardingStep.PhoneEntry ->
                PhoneStep(
                    modifier = contentModifier,
                    selectedCountry = selectedCountry,
                    phoneNumber = phoneNumber,
                    phoneError = phoneInputError,
                    isLoading = false,
                    onPickCountry = { showCountryPicker = true },
                    onPhoneChanged = { input ->
                        phoneNumber = input.filter(Char::isDigit).take(20)
                        phoneInputError = null
                    },
                    onContinue = {
                        val candidate = phoneNumber.ifBlank { phoneOverride.orEmpty() }
                        if (!candidate.isDigitsOnly() || candidate.length < 7) {
                            phoneInputError = strings.onboarding.invalidPhoneError
                            return@PhoneStep
                        }
                        phoneNumber = candidate
                        phoneInputError = null
                        otp = ""
                        otpError = null
                        step = OnboardingStep.OTP
                    },
                    continueEnabled = true,
                )

            OnboardingStep.OTP ->
                OTPStep(
                    modifier = contentModifier,
                    otp = otp,
                    countdown = 0,
                    canResend = false,
                    isRequesting = false,
                    isVerifying = false,
                    errorMessage = otpError,
                    onOtpChanged = { value ->
                        if (value.length <= 6 && value.all(Char::isDigit)) {
                            otp = value
                            otpError = null
                        }
                    },
                    onResend = {},
                    onVerify = {
                        val candidate = otp.ifBlank { otpOverride.orEmpty() }
                        if (candidate.length == 6) {
                            if (candidate == UI_TEST_OTP) {
                                step = OnboardingStep.Success
                            } else {
                                otpError = strings.onboarding.invalidVerificationCode
                            }
                        }
                    },
                    verifyEnabled = true,
                )

            OnboardingStep.Success -> SuccessStep(onFinished = onFinished)
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentSelection = selectedCountry,
            onDismiss = { showCountryPicker = false },
            onSelect = {
                selectedCountryCode = it.isoCode
                showCountryPicker = false
            },
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun OnboardingFlowScreenPreview() {
    LiveChatPreviewContainer {
        OnboardingFlowScreen(onFinished = {})
    }
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

private const val UI_TEST_OTP = "123123"
