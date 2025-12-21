package com.edufelip.livechat.ui.features.conversations

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class ConversationDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPreviewMessages() {
        val state = PreviewFixtures.conversationUiState
        composeRule.setContent {
            LiveChatTheme {
                ConversationDetailScreen(
                    state = state,
                    contactName = "Preview Contact",
                    currentUserId = "preview-user",
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
                    permissionHint = null,
                )
            }
        }

        composeRule.onNodeWithText(state.messages.first().body).assertIsDisplayed()
        composeRule.onNodeWithText(state.messages.last().body).assertIsDisplayed()
    }
}
