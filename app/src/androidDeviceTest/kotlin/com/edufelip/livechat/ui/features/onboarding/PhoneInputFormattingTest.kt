package com.edufelip.livechat.ui.features.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import com.edufelip.livechat.ui.util.phoneNumberFormattingService
import com.edufelip.livechat.ui.util.rememberPhoneNumberFormattingService
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class PhoneInputFormattingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun phoneInputFormatsAsYouTypeForSelectedCountry() {
        val rawDigits = "6505553434"
        val expected = phoneNumberFormattingService().formatAsYouType(rawDigits, "US")
        assertNotEquals(rawDigits, expected)

        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val formatter = rememberPhoneNumberFormattingService()
                val country =
                    CountryOption.fromIsoCode(
                        strings.onboarding.defaultCountryIso,
                        strings.onboarding.priorityCountryIsos,
                        strings.onboarding.defaultCountryIso,
                    )
                var phoneDigits by remember { mutableStateOf("") }
                val formatted = formatter.formatAsYouType(phoneDigits, country.isoCode)
                PhoneStep(
                    selectedCountry = country,
                    phoneNumber = formatted,
                    phoneError = null,
                    isLoading = false,
                    phonePlaceholder = formatter.exampleNumber(country.isoCode),
                    onPickCountry = {},
                    onPhoneChanged = { input ->
                        phoneDigits = formatter.normalizeDigits(input).take(20)
                    },
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_INPUT).performTextInput(rawDigits)
        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_INPUT).assertTextEquals(expected)
    }
}
