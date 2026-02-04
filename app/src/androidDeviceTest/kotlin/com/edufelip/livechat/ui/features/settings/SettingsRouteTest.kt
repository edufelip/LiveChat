package com.edufelip.livechat.ui.features.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.features.settings.model.SettingsSection
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertEquals
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
        var lastRequest: SettingsNavigationRequest? = null

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
                val accountScreenTitleLocal = settingsStrings.accountTitle
                val notificationsScreenTitleLocal = settingsStrings.notificationsTitle
                val appearanceScreenTitleLocal = settingsStrings.appearanceTitle
                val privacyScreenTitleLocal = settingsStrings.privacyTitle

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
                    onSectionSelected = { request -> lastRequest = request },
                )
            }
        }

        composeRule.waitForIdle()

        assertSectionSelection(
            sectionTitle = accountTitle,
            expectedSection = SettingsSection.Account,
            expectedDescription = accountDescription,
            expectedTitle = accountScreenTitle,
            lastRequestProvider = { lastRequest },
        )
        assertSectionSelection(
            sectionTitle = notificationsTitle,
            expectedSection = SettingsSection.Notifications,
            expectedDescription = notificationsDescription,
            expectedTitle = notificationsScreenTitle,
            lastRequestProvider = { lastRequest },
        )
        assertSectionSelection(
            sectionTitle = appearanceTitle,
            expectedSection = SettingsSection.Appearance,
            expectedDescription = appearanceDescription,
            expectedTitle = appearanceScreenTitle,
            lastRequestProvider = { lastRequest },
        )
        assertSectionSelection(
            sectionTitle = privacyTitle,
            expectedSection = SettingsSection.Privacy,
            expectedDescription = privacyDescription,
            expectedTitle = privacyScreenTitle,
            lastRequestProvider = { lastRequest },
        )
    }

    private fun assertSectionSelection(
        sectionTitle: String,
        expectedSection: SettingsSection,
        expectedDescription: String,
        expectedTitle: String,
        lastRequestProvider: () -> SettingsNavigationRequest?,
    ) {
        composeRule.onNodeWithText(sectionTitle).performClick()
        composeRule.runOnIdle {
            val request = requireNotNull(lastRequestProvider()) { "Expected selection for $sectionTitle" }
            assertEquals(expectedSection, request.section)
            assertEquals(expectedTitle, request.title)
            assertEquals(expectedDescription, request.description)
        }
    }
}
