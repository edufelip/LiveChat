package com.edufelip.livechat.ui.features.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class SettingsRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigatesToEachSettingsSection() {
        var accountTitle = ""
        var accountDescription = ""
        var notificationsTitle = ""
        var notificationsDescription = ""
        var appearanceTitle = ""
        var appearanceDescription = ""
        var privacyTitle = ""
        var privacyDescription = ""
        var accountScreenTitle = ""
        var notificationsScreenTitle = ""
        var appearanceScreenTitle = ""
        var privacyScreenTitle = ""
        val backLabel = "Back"

        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val settingsStrings = strings.settings
                val accountTitleLocal = settingsStrings.accountTitle
                val accountDescriptionLocal = settingsStrings.accountDescription
                val notificationsTitleLocal = settingsStrings.notificationsTitle
                val notificationsDescriptionLocal = settingsStrings.notificationsDescription
                val appearanceTitleLocal = settingsStrings.appearanceTitle
                val appearanceDescriptionLocal = settingsStrings.appearanceDescription
                val privacyTitleLocal = settingsStrings.privacyTitle
                val privacyDescriptionLocal = settingsStrings.privacyDescription
                val accountScreenTitleLocal = strings.account.screenTitle
                val notificationsScreenTitleLocal = strings.notifications.screenTitle
                val appearanceScreenTitleLocal = strings.appearance.screenTitle
                val privacyScreenTitleLocal = strings.privacy.screenTitle

                SideEffect {
                    accountTitle = accountTitleLocal
                    accountDescription = accountDescriptionLocal
                    notificationsTitle = notificationsTitleLocal
                    notificationsDescription = notificationsDescriptionLocal
                    appearanceTitle = appearanceTitleLocal
                    appearanceDescription = appearanceDescriptionLocal
                    privacyTitle = privacyTitleLocal
                    privacyDescription = privacyDescriptionLocal
                    accountScreenTitle = accountScreenTitleLocal
                    notificationsScreenTitle = notificationsScreenTitleLocal
                    appearanceScreenTitle = appearanceScreenTitleLocal
                    privacyScreenTitle = privacyScreenTitleLocal
                }

                SettingsRoute(
                    modifier = Modifier.fillMaxSize(),
                    accountContent = { modifier, onBack ->
                        SettingsTestScreen(
                            modifier = modifier,
                            title = accountScreenTitleLocal,
                            backLabel = backLabel,
                            onBack = onBack,
                        )
                    },
                    notificationsContent = { modifier, onBack ->
                        SettingsTestScreen(
                            modifier = modifier,
                            title = notificationsScreenTitleLocal,
                            backLabel = backLabel,
                            onBack = onBack,
                        )
                    },
                    appearanceContent = { modifier, onBack ->
                        SettingsTestScreen(
                            modifier = modifier,
                            title = appearanceScreenTitleLocal,
                            backLabel = backLabel,
                            onBack = onBack,
                        )
                    },
                    privacyContent = { modifier, onBack ->
                        SettingsTestScreen(
                            modifier = modifier,
                            title = privacyScreenTitleLocal,
                            backLabel = backLabel,
                            onBack = onBack,
                        )
                    },
                )
            }
        }

        composeRule.waitForIdle()

        assertSectionNavigation(
            sectionTitle = accountTitle,
            sectionDescription = accountDescription,
            destinationTitle = accountScreenTitle,
            backLabel = backLabel,
        )
        assertSectionNavigation(
            sectionTitle = notificationsTitle,
            sectionDescription = notificationsDescription,
            destinationTitle = notificationsScreenTitle,
            backLabel = backLabel,
        )
        assertSectionNavigation(
            sectionTitle = appearanceTitle,
            sectionDescription = appearanceDescription,
            destinationTitle = appearanceScreenTitle,
            backLabel = backLabel,
        )
        assertSectionNavigation(
            sectionTitle = privacyTitle,
            sectionDescription = privacyDescription,
            destinationTitle = privacyScreenTitle,
            backLabel = backLabel,
        )
    }

    private fun assertSectionNavigation(
        sectionTitle: String,
        sectionDescription: String,
        destinationTitle: String,
        backLabel: String,
    ) {
        composeRule.onNodeWithText(sectionTitle).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(sectionDescription).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText(destinationTitle).assertIsDisplayed()
        composeRule.onNodeWithText(backLabel).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(sectionDescription).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

@Composable
private fun SettingsTestScreen(
    modifier: Modifier,
    title: String,
    backLabel: String,
    onBack: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(text = title)
        Text(
            text = backLabel,
            modifier = Modifier.clickable(onClick = onBack),
        )
    }
}
