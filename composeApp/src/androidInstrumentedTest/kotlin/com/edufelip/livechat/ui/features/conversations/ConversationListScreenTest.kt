package com.edufelip.livechat.ui.features.conversations

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.list.screens.ConversationListScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConversationListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPinnedAndConversationNames() {
        var pinnedLabel = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings().conversation
                SideEffect { pinnedLabel = strings.pinnedSectionTitle }
                ConversationListScreen(
                    state = PreviewFixtures.conversationListState,
                    onSearch = {},
                    onConversationSelected = {},
                    onTogglePin = { _, _ -> },
                    onToggleMute = { _, _ -> },
                    onToggleArchive = { _, _ -> },
                    onFilterSelected = {},
                )
            }
        }

        composeRule.waitForIdle()
        val pinnedHeaders = composeRule.onAllNodesWithText(pinnedLabel).fetchSemanticsNodes()
        assertTrue(pinnedHeaders.isNotEmpty())
        composeRule.onNodeWithText("Ava Harper").assertIsDisplayed()
        composeRule.onNodeWithText("Brandon Diaz").assertIsDisplayed()
    }
}
