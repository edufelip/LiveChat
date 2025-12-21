package com.edufelip.livechat.ui.features.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.edufelip.livechat.ui.features.onboarding.steps.OTPStep
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OTPStepTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun verifyDisabledUntilSixDigits() {
        var verifyTapped = 0
        composeRule.setContent {
            var otp by mutableStateOf("")
            LiveChatTheme {
                OTPStep(
                    otp = otp,
                    countdown = 30,
                    canResend = false,
                    isRequesting = false,
                    isVerifying = false,
                    errorMessage = null,
                    onOtpChanged = { otp = it },
                    onResend = {},
                    onVerify = { verifyTapped += 1 },
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextInput("12345")
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextClearance()
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextInput("123123")
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).assertIsEnabled()
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).performClick()
        composeRule.runOnIdle {
            assertEquals(1, verifyTapped)
        }
    }

    @Test
    fun showsResendButtonWhenAllowed() {
        composeRule.setContent {
            LiveChatTheme {
                OTPStep(
                    otp = "",
                    countdown = 0,
                    canResend = true,
                    isRequesting = false,
                    isVerifying = false,
                    errorMessage = null,
                    onOtpChanged = {},
                    onResend = {},
                    onVerify = {},
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.OTP_RESEND_BUTTON).assertIsDisplayed()
    }

    @Test
    fun showsErrorMessageWhenProvided() {
        val errorMessage = "Invalid code"
        composeRule.setContent {
            LiveChatTheme {
                OTPStep(
                    otp = "123456",
                    countdown = 0,
                    canResend = false,
                    isRequesting = false,
                    isVerifying = false,
                    errorMessage = errorMessage,
                    onOtpChanged = {},
                    onResend = {},
                    onVerify = {},
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
