package com.edufelip.livechat.ui.features.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.edufelip.livechat.ui.features.settings.screens.SettingsScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchFiltersSettingsRows() {
        var accountTitle = ""
        var notificationsTitle = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                SideEffect {
                    accountTitle = strings.settings.accountTitle
                    notificationsTitle = strings.settings.notificationsTitle
                }
                SettingsScreen()
            }
        }

        composeRule.onNodeWithText(accountTitle).assertIsDisplayed()
        composeRule.onNodeWithText(notificationsTitle).assertIsDisplayed()

        composeRule.onNodeWithTag(SettingsTestTags.SEARCH_FIELD).performTextInput(accountTitle.take(3))
        composeRule.onNodeWithText(accountTitle).assertIsDisplayed()
        composeRule.onNodeWithText(notificationsTitle).assertDoesNotExist()
    }
}
