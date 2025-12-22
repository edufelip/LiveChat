package com.edufelip.livechat.ui.features.contacts

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContactsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsRegisteredAndInviteSections() {
        var registeredLabel = ""
        var inviteSectionTitle = ""
        var inviteCta = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings().contacts
                SideEffect {
                    registeredLabel = strings.registeredSectionTitle
                    inviteSectionTitle = strings.inviteSectionTitle
                    inviteCta = strings.inviteCta
                }
                ContactsScreen(
                    state = PreviewFixtures.contactsState,
                    onInvite = {},
                    onContactSelected = {},
                    onSync = {},
                    onDismissError = {},
                )
            }
        }

        composeRule.waitForIdle()
        val registeredBadges = composeRule.onAllNodesWithText(registeredLabel).fetchSemanticsNodes()
        assertTrue(registeredBadges.isNotEmpty())
        composeRule.onNodeWithText(inviteSectionTitle).assertIsDisplayed()
        val inviteButtons = composeRule.onAllNodesWithText(inviteCta).fetchSemanticsNodes()
        assertTrue(inviteButtons.isNotEmpty())
        composeRule.onNodeWithText("Ava Harper").assertIsDisplayed()
        composeRule.onNodeWithText("Chioma Ade").assertIsDisplayed()
    }
}
