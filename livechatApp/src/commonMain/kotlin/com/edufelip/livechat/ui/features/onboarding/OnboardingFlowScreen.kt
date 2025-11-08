package com.edufelip.livechat.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.dialogs.CountryPickerDialog
import com.edufelip.livechat.ui.features.onboarding.steps.OTPStep
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.features.onboarding.steps.SuccessStep
import com.edufelip.livechat.ui.resources.OnboardingStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberPhoneAuthPresenter
import com.edufelip.livechat.ui.util.isDigitsOnly
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.auth.phone.model.phoneAuthPresentationContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingFlowScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
    val context = LocalContext.current

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

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (currentStep) {
            OnboardingStep.PhoneEntry ->
                PhoneStep(
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
                        if (inspectionMode) return@PhoneStep
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
                        if (!inspectionMode) {
                            phoneAuthState.session?.let {
                                val presentationContext =
                                    runCatching { phoneAuthPresentationContext(context) }.getOrNull()
                                        ?: return@OTPStep
                                phoneAuthPresenter.resendCode(presentationContext)
                            }
                        }
                    },
                    onVerify = {
                        if (!inspectionMode && otp.length == 6) {
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
