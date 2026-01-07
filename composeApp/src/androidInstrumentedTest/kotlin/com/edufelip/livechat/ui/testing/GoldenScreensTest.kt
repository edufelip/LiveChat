package com.edufelip.livechat.ui.testing

import android.os.SystemClock
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.features.conversations.list.screens.ConversationListScreen
import com.edufelip.livechat.ui.features.onboarding.CountryOption
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.features.onboarding.steps.OTPStep
import com.edufelip.livechat.ui.features.onboarding.steps.PhoneStep
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class GoldenScreensTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun goldenPhoneStep() {
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
                    phoneNumber = "6505553434",
                    phoneError = null,
                    isLoading = false,
                    onPickCountry = {},
                    onPhoneChanged = {},
                    onContinue = {},
                )
            }
        }
        GoldenAssertions.assertGolden(composeRule, "phone_step")
    }

    @Test
    fun goldenPhoneStepWithKeyboard() {
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
                    phoneNumber = "6505553434",
                    phoneError = null,
                    isLoading = false,
                    onPickCountry = {},
                    onPhoneChanged = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.PHONE_INPUT).performClick()
        composeRule.runOnIdle {
            val activity = composeRule.activity
            val inputMethodManager = activity.getSystemService(InputMethodManager::class.java)
            val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
            val composeView = contentView.getChildAt(0) ?: contentView
            composeView.requestFocus()
            inputMethodManager?.showSoftInput(composeView, InputMethodManager.SHOW_IMPLICIT)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(750)

        DeviceGoldenAssertions.assertDeviceGolden("phone_step_keyboard")
    }

    @Test
    fun goldenOtpStep() {
        composeRule.setContent {
            LiveChatTheme {
                OTPStep(
                    otp = "123123",
                    countdown = 20,
                    canResend = false,
                    isRequesting = false,
                    isVerifying = false,
                    errorMessage = null,
                    onOtpChanged = {},
                    onResend = {},
                    onVerify = {},
                )
            }
        }
        GoldenAssertions.assertGolden(composeRule, "otp_step")
    }

    @Test
    fun goldenConversationList() {
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
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
        GoldenAssertions.assertGolden(composeRule, "conversation_list")
    }

    @Test
    fun goldenContacts() {
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                ContactsScreen(
                    state = PreviewFixtures.contactsState(strings),
                    onInvite = {},
                    onContactSelected = {},
                    onSync = {},
                    onSearchQueryChange = {},
                    onDismissError = {},
                    onBack = {},
                )
            }
        }
        GoldenAssertions.assertGolden(composeRule, "contacts")
    }

    @Test
    fun goldenConversationDetail() {
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val state = PreviewFixtures.conversationUiState(strings)
                val snackbarHostState = remember { SnackbarHostState() }
                ConversationDetailScreen(
                    state = state,
                    contactName = strings.preview.contactPrimaryName,
                    currentUserId = PreviewFixtures.previewUserId(),
                    onSendMessage = {},
                    isRecording = false,
                    recordingDurationMillis = 0L,
                    onStartRecording = {},
                    onCancelRecording = {},
                    onSendRecording = {},
                    onPickImage = {},
                    onTakePhoto = {},
                    onBack = {},
                    onDismissError = {},
                    onMessageErrorClick = {},
                    snackbarHostState = snackbarHostState,
                    selectedMessage = null,
                    selectedMessageBounds = null,
                    scrollToBottomSignal = 0,
                    onMessageLongPress = { _, _ -> },
                    onDismissMessageActions = {},
                    onCopyMessage = {},
                    onDeleteMessage = {},
                    onRetryMessage = {},
                    permissionHint = null,
                )
            }
        }
        GoldenAssertions.assertGolden(composeRule, "conversation_detail")
    }
}
