package com.project.livechat.composeapp.ui.features.onboarding

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
import androidx.compose.ui.platform.LocalInspectionMode
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.features.onboarding.dialogs.CountryPickerDialog
import com.project.livechat.composeapp.ui.features.onboarding.steps.OTPStep
import com.project.livechat.composeapp.ui.features.onboarding.steps.PhoneStep
import com.project.livechat.composeapp.ui.features.onboarding.steps.SuccessStep
import com.project.livechat.composeapp.ui.util.isDigitsOnly
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingFlowScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.PhoneEntry) }
    var selectedCountry by rememberSaveable { mutableStateOf(CountryOption.default()) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var countdown by rememberSaveable { mutableStateOf(60) }
    var timerActive by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current

    LaunchedEffect(timerActive, countdown, inspectionMode) {
        if (!inspectionMode && timerActive && countdown > 0) {
            while (countdown > 0 && timerActive) {
                delay(1_000)
                countdown -= 1
            }
            if (countdown <= 0) {
                timerActive = false
            }
        }
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (step) {
            OnboardingStep.PhoneEntry ->
                PhoneStep(
                    selectedCountry = selectedCountry,
                    phoneNumber = phoneNumber,
                    phoneError = phoneError,
                    onPickCountry = { showCountryPicker = true },
                    onPhoneChanged = { input ->
                        phoneNumber = input.filter(Char::isDigit).take(20)
                        phoneError = null
                    },
                    onContinue = {
                        if (!phoneNumber.isDigitsOnly() || phoneNumber.length < 7) {
                            phoneError = "Please enter a valid phone number"
                            return@PhoneStep
                        }
                        step = OnboardingStep.OTP
                        otp = ""
                        countdown = 60
                        timerActive = true
                        phoneError = null
                    },
                )

            OnboardingStep.OTP ->
                OTPStep(
                    otp = otp,
                    countdown = countdown,
                    timerActive = timerActive,
                    onOtpChanged = { value ->
                        if (value.length <= 6 && value.all(Char::isDigit)) {
                            otp = value
                        }
                    },
                    onResend = {
                        countdown = 60
                        timerActive = true
                    },
                    onVerify = {
                        if (otp.length == 6) {
                            timerActive = false
                            step = OnboardingStep.Success
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
                selectedCountry = it
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
