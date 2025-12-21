package com.edufelip.livechat.ui.features.conversations

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.list.screens.ConversationListScreen
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConversationListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPinnedAndConversationNames() {
        composeRule.setContent {
            LiveChatTheme {
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

        val pinnedHeaders = composeRule.onAllNodesWithText("Pinned").fetchSemanticsNodes()
        assertTrue(pinnedHeaders.isNotEmpty())
        composeRule.onNodeWithText("Ava Harper").assertIsDisplayed()
        composeRule.onNodeWithText("Brandon Diaz").assertIsDisplayed()
    }
}
