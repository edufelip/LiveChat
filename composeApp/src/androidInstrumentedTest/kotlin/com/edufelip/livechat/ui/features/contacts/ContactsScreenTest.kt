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
        var primaryName = ""
        var tertiaryName = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val preview = strings.preview
                SideEffect {
                    registeredLabel = strings.contacts.registeredSectionTitle
                    inviteSectionTitle = strings.contacts.inviteSectionTitle
                    inviteCta = strings.contacts.inviteCta
                    primaryName = preview.contactPrimaryName
                    tertiaryName = preview.contactTertiaryName
                }
                ContactsScreen(
                    state = PreviewFixtures.contactsState(strings),
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
        composeRule.onNodeWithText(primaryName).assertIsDisplayed()
        composeRule.onNodeWithText(tertiaryName).assertIsDisplayed()
    }
}
