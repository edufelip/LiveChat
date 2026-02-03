package com.edufelip.livechat.ui.features.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PhoneStepTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun continueDisabledUntilPhoneProvided() {
        var continueTapped = 0
        composeRule.setContent {
            var phoneNumber by remember { mutableStateOf("") }
            LiveChatTheme {
                val strings = liveChatStrings()
                PhoneStep(
                    selectedCountry =
                        CountryOption.fromIsoCode(
                            strings.onboarding.defaultCountryIso,
                            strings.onboarding.priorityCountryIsos,
                            strings.onboarding.defaultCountryIso,
                        ),
                    phoneNumber = phoneNumber,
                    phoneError = null,
                    isLoading = false,
                    onPickCountry = {},
                    onPhoneChanged = { phoneNumber = it },
                    onContinue = { continueTapped += 1 },
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_INPUT).performTextInput("6505553434")
        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON).assertIsEnabled()
        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON).performClick()
        composeRule.runOnIdle {
            assertEquals(1, continueTapped)
        }
    }

    @Test
    fun countrySelectorInvokesCallback() {
        var taps = 0
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                PhoneStep(
                    selectedCountry =
                        CountryOption.default(
                            strings.onboarding.priorityCountryIsos,
                            strings.onboarding.defaultCountryIso,
                        ),
                    phoneNumber = "5550100",
                    phoneError = null,
                    isLoading = false,
                    onPickCountry = { taps += 1 },
                    onPhoneChanged = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_COUNTRY_SELECTOR).performClick()
        composeRule.runOnIdle {
            assertEquals(1, taps)
        }
    }

    @Test
    fun showsPhoneErrorMessage() {
        var errorMessage = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                SideEffect { errorMessage = strings.onboarding.invalidPhoneError }
                PhoneStep(
                    selectedCountry =
                        CountryOption.default(
                            strings.onboarding.priorityCountryIsos,
                            strings.onboarding.defaultCountryIso,
                        ),
                    phoneNumber = "123",
                    phoneError = errorMessage,
                    isLoading = false,
                    onPickCountry = {},
                    onPhoneChanged = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
