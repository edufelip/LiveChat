package com.edufelip.livechat.ui.features.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class PhoneStepPlaceholderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun phoneStepShowsCustomPlaceholder() {
        val placeholder = "(201) 555-0123"
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                PhoneStep(
                    selectedCountry =
                        CountryOption.fromIsoCode(
                            strings.onboarding.defaultCountryIso,
                            strings.onboarding.priorityCountryIsos,
                            strings.onboarding.defaultCountryIso,
                        ),
                    phoneNumber = "",
                    phoneError = null,
                    isLoading = false,
                    phonePlaceholder = placeholder,
                    onPickCountry = {},
                    onPhoneChanged = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
    }
}
