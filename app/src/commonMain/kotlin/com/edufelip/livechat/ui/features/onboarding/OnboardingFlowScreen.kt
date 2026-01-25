package com.edufelip.livechat.ui.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.rememberUpdatedState
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
import com.edufelip.livechat.ui.platform.isAndroid
import com.edufelip.livechat.ui.platform.rememberPlatformContext
import com.edufelip.livechat.ui.resources.OnboardingStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberPhoneAuthPresenter
import com.edufelip.livechat.ui.theme.LocalReduceMotion
import com.edufelip.livechat.ui.util.isDigitsOnly
import com.edufelip.livechat.ui.util.isUiTestMode
import com.edufelip.livechat.ui.util.uiTestOverrides
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalAnimationApi::class)
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
    val onboardingConfig = strings.onboarding
    val priorityIsoCodes = onboardingConfig.priorityCountryIsos
    val defaultCountryIso = onboardingConfig.defaultCountryIso
    val phoneAuthPresenter = rememberPhoneAuthPresenter()
    val phoneAuthState by phoneAuthPresenter.collectState()
    var selectedCountryCode by rememberSaveable { mutableStateOf(defaultCountryIso) }
    val selectedCountry =
        remember(selectedCountryCode, priorityIsoCodes, defaultCountryIso) {
            CountryOption.fromIsoCode(selectedCountryCode, priorityIsoCodes, defaultCountryIso)
        }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneInputError by remember { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current
    val context = rememberPlatformContext()
    val allowVerification = !inspectionMode
    val reduceMotion = LocalReduceMotion.current
    val onFinishedAction = rememberStableAction(onFinished)
    val onPickCountryAction = rememberStableAction { showCountryPicker = true }
    val onDismissCountryPickerAction = rememberStableAction { showCountryPicker = false }
    val onSelectCountryAction =
        rememberStableAction<CountryOption> {
            selectedCountryCode = it.isoCode
            showCountryPicker = false
        }
    val onPhoneChangedAction =
        rememberStableAction<String> { input ->
            phoneNumber = input.filter(Char::isDigit).take(20)
            phoneInputError = null
            phoneAuthPresenter.dismissError()
        }
    val onContinueAction =
        rememberStableAction {
            if (!phoneNumber.isDigitsOnly() || phoneNumber.length < 7) {
                phoneInputError = strings.onboarding.invalidPhoneError
                return@rememberStableAction
            }
            if (!allowVerification) return@rememberStableAction
            val presentationContext = runCatching { phoneAuthPresentationContext(context) }.getOrNull()
            if (presentationContext == null) {
                phoneInputError = strings.onboarding.startVerificationError
                return@rememberStableAction
            }
            phoneAuthPresenter.startVerification(
                PhoneNumber(
                    dialCode = selectedCountry.dialCode,
                    nationalNumber = phoneNumber,
                ),
                presentationContext,
            )
        }
    val onOtpChangedAction =
        rememberStableAction<String> { value ->
            if (value.length <= 6 && value.all(Char::isDigit)) {
                otp = value
                phoneAuthPresenter.dismissError()
            }
        }
    val onResendAction =
        rememberStableAction {
            if (!allowVerification) return@rememberStableAction
            if (phoneAuthState.session == null) return@rememberStableAction
            val presentationContext =
                runCatching { phoneAuthPresentationContext(context) }.getOrNull()
                    ?: return@rememberStableAction
            phoneAuthPresenter.resendCode(presentationContext)
        }
    val onVerifyAction =
        rememberStableAction {
            if (allowVerification && otp.length == 6) {
                phoneAuthPresenter.verifyCode(otp)
            }
        }

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
        contentWindowInsets =
            WindowInsets.safeDrawing
                .only(WindowInsetsSides.Bottom)
                .exclude(WindowInsets.ime),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .let { baseModifier ->
                    if (isAndroid()) {
                        baseModifier.imePadding()
                    } else {
                        baseModifier
                    }
                }
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                } else {
                    val direction =
                        when {
                            targetState.animationOrder() > initialState.animationOrder() -> 1
                            targetState.animationOrder() < initialState.animationOrder() -> -1
                            else -> 0
                        }
                    if (direction == 0) {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                    } else {
                        (
                            slideInHorizontally(
                                animationSpec = tween(300),
                            ) { fullWidth -> fullWidth / 4 * direction } + fadeIn(animationSpec = tween(300))
                        ) togetherWith
                            (
                                slideOutHorizontally(
                                    animationSpec = tween(300),
                                ) { fullWidth -> -fullWidth / 4 * direction } + fadeOut(animationSpec = tween(200))
                            )
                    }
                }
            },
            label = strings.general.homeDestinationTransitionLabel,
        ) { step ->
            when (step) {
                OnboardingStep.PhoneEntry ->
                    PhoneStep(
                        modifier = contentModifier,
                        selectedCountry = selectedCountry,
                        phoneNumber = phoneNumber,
                        phoneError = phoneErrorMessage,
                        isLoading = phoneAuthState.isRequesting,
                        onPickCountry = onPickCountryAction,
                        onPhoneChanged = onPhoneChangedAction,
                        onContinue = onContinueAction,
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
                        onOtpChanged = onOtpChangedAction,
                        onResend = onResendAction,
                        onVerify = onVerifyAction,
                    )

                OnboardingStep.Success -> SuccessStep(onFinished = onFinishedAction)
            }
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentSelection = selectedCountry,
            onDismiss = onDismissCountryPickerAction,
            onSelect = onSelectCountryAction,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun UiTestOnboardingFlow(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val onboardingConfig = strings.onboarding
    val priorityIsoCodes = onboardingConfig.priorityCountryIsos
    val defaultCountryIso = onboardingConfig.defaultCountryIso
    val overrides = uiTestOverrides()
    val phoneOverride = remember(overrides.phone) { overrides.phone?.filter(Char::isDigit) }
    val otpOverride = remember(overrides.otp) { overrides.otp?.filter(Char::isDigit)?.take(6) }
    var selectedCountryCode by rememberSaveable { mutableStateOf(defaultCountryIso) }
    val selectedCountry =
        remember(selectedCountryCode, priorityIsoCodes, defaultCountryIso) {
            CountryOption.fromIsoCode(selectedCountryCode, priorityIsoCodes, defaultCountryIso)
        }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneInputError by remember { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var step by rememberSaveable { mutableStateOf(OnboardingStep.PhoneEntry) }
    val reduceMotion = LocalReduceMotion.current
    val onFinishedAction = rememberStableAction(onFinished)
    val onPickCountryAction = rememberStableAction { showCountryPicker = true }
    val onDismissCountryPickerAction = rememberStableAction { showCountryPicker = false }
    val onSelectCountryAction =
        rememberStableAction<CountryOption> {
            selectedCountryCode = it.isoCode
            showCountryPicker = false
        }
    val onPhoneChangedAction =
        rememberStableAction<String> { input ->
            phoneNumber = input.filter(Char::isDigit).take(20)
            phoneInputError = null
        }
    val onContinueAction =
        rememberStableAction {
            val candidate = phoneNumber.ifBlank { phoneOverride.orEmpty() }
            if (!candidate.isDigitsOnly() || candidate.length < 7) {
                phoneInputError = strings.onboarding.invalidPhoneError
                return@rememberStableAction
            }
            phoneNumber = candidate
            phoneInputError = null
            otp = ""
            otpError = null
            step = OnboardingStep.OTP
        }
    val onOtpChangedAction =
        rememberStableAction<String> { value ->
            if (value.length <= 6 && value.all(Char::isDigit)) {
                otp = value
                otpError = null
            }
        }
    val onVerifyAction =
        rememberStableAction {
            val candidate = otp.ifBlank { otpOverride.orEmpty() }
            if (candidate.length == 6) {
                if (candidate == UI_TEST_OTP) {
                    step = OnboardingStep.Success
                } else {
                    otpError = strings.onboarding.invalidVerificationCode
                }
            }
        }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.UI_TEST_MODE),
        contentWindowInsets =
            WindowInsets.safeDrawing
                .only(WindowInsetsSides.Bottom)
                .exclude(WindowInsets.ime),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .let { baseModifier ->
                    if (isAndroid()) {
                        baseModifier.imePadding()
                    } else {
                        baseModifier
                    }
                }
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                } else {
                    val direction =
                        when {
                            targetState.animationOrder() > initialState.animationOrder() -> 1
                            targetState.animationOrder() < initialState.animationOrder() -> -1
                            else -> 0
                        }
                    if (direction == 0) {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                    } else {
                        (
                            slideInHorizontally(
                                animationSpec = tween(300),
                            ) { fullWidth -> fullWidth / 4 * direction } + fadeIn(animationSpec = tween(300))
                        ) togetherWith
                            (
                                slideOutHorizontally(
                                    animationSpec = tween(300),
                                ) { fullWidth -> -fullWidth / 4 * direction } + fadeOut(animationSpec = tween(200))
                            )
                    }
                }
            },
            label = strings.general.homeDestinationTransitionLabel,
        ) { target ->
            when (target) {
                OnboardingStep.PhoneEntry ->
                    PhoneStep(
                        modifier = contentModifier,
                        selectedCountry = selectedCountry,
                        phoneNumber = phoneNumber,
                        phoneError = phoneInputError,
                        isLoading = false,
                        onPickCountry = onPickCountryAction,
                        onPhoneChanged = onPhoneChangedAction,
                        onContinue = onContinueAction,
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
                        onOtpChanged = onOtpChangedAction,
                        onResend = {},
                        onVerify = onVerifyAction,
                        verifyEnabled = true,
                    )

                OnboardingStep.Success -> SuccessStep(onFinished = onFinishedAction)
            }
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentSelection = selectedCountry,
            onDismiss = onDismissCountryPickerAction,
            onSelect = onSelectCountryAction,
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

private fun OnboardingStep.animationOrder(): Int =
    when (this) {
        OnboardingStep.PhoneEntry -> 0
        OnboardingStep.OTP -> 1
        OnboardingStep.Success -> 2
    }

private const val UI_TEST_OTP = "123123"
