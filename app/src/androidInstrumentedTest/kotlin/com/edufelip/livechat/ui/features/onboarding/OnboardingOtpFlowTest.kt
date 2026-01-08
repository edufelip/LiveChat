package com.edufelip.livechat.ui.features.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.list.screens.ConversationListScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class OnboardingOtpFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun invalidOtpShowsError_thenSuccessNavigatesToConversationList() {
        var invalidOtpMessage = ""
        var finishedCalled = false
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                SideEffect {
                    invalidOtpMessage = strings.onboarding.invalidVerificationCode
                }
                var destination by remember { mutableStateOf(OnboardingDestination.Onboarding) }
                when (destination) {
                    OnboardingDestination.Onboarding ->
                        UiTestOnboardingFlow(
                            onFinished = {
                                finishedCalled = true
                                destination = OnboardingDestination.Home
                            },
                        )
                    OnboardingDestination.Home ->
                        Box(modifier = Modifier.testTag(HOME_DESTINATION_TAG)) {
                            ConversationListScreen(
                                state = PreviewFixtures.conversationListState(strings),
                                onSearch = {},
                                onConversationSelected = {},
                                onTogglePin = { _, _ -> },
                                onToggleMute = { _, _ -> },
                                onToggleArchive = { _, _ -> },
                                onFilterSelected = {},
                                onCompose = {},
                                onEmptyStateAction = {},
                            )
                        }
                }
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_INPUT).performTextInput("6505553434")
        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON).performClick()

        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextInput("000000")
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).performClick()
        composeRule.onNodeWithText(invalidOtpMessage).assertIsDisplayed()

        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextClearance()
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_INPUT).performTextInput("123123")
        composeRule.onNodeWithTag(OnboardingTestTags.OTP_VERIFY_BUTTON).performClick()
        composeRule.onNodeWithTag(OnboardingTestTags.SUCCESS_STEP).assertIsDisplayed()

        composeRule.onNodeWithTag(OnboardingTestTags.SUCCESS_BUTTON).performClick()
        composeRule.runOnIdle {
            check(finishedCalled) { "Expected onFinished to be called after tapping success CTA." }
        }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(HOME_DESTINATION_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(HOME_DESTINATION_TAG).assertIsDisplayed()
    }

    private enum class OnboardingDestination {
        Onboarding,
        Home,
    }

    private companion object {
        const val HOME_DESTINATION_TAG = "home_destination"
    }
}
