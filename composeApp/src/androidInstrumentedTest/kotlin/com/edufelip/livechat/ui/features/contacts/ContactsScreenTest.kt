package com.edufelip.livechat.ui.features.contacts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContactsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsRegisteredAndInviteSections() {
        composeRule.setContent {
            LiveChatTheme {
                ContactsScreen(
                    state = PreviewFixtures.contactsState,
                    onInvite = {},
                    onContactSelected = {},
                    onSync = {},
                    onDismissError = {},
                )
            }
        }

        val registeredBadges = composeRule.onAllNodesWithText("On LiveChat").fetchSemanticsNodes()
        assertTrue(registeredBadges.isNotEmpty())
        composeRule.onNodeWithText("Invite to LiveChat").assertIsDisplayed()
        val inviteButtons = composeRule.onAllNodesWithText("Invite").fetchSemanticsNodes()
        assertTrue(inviteButtons.isNotEmpty())
        composeRule.onNodeWithText("Ava Harper").assertIsDisplayed()
        composeRule.onNodeWithText("Chioma Ade").assertIsDisplayed()
    }
}
