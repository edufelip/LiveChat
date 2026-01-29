package com.edufelip.livechat.ui.features.conversations

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.conversations.detail.screens.ConversationDetailScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class ConversationDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsPreviewMessages() {
        var firstMessage = ""
        var lastMessage = ""
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val state = PreviewFixtures.conversationUiState(strings)
                val snackbarHostState = remember { SnackbarHostState() }
                SideEffect {
                    firstMessage = state.messages.firstOrNull()?.body.orEmpty()
                    lastMessage = state.messages.lastOrNull()?.body.orEmpty()
                }
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

        composeRule.onNodeWithText(firstMessage).assertIsDisplayed()
        composeRule.onNodeWithText(lastMessage).assertIsDisplayed()
    }
}
