package com.project.livechat.composeapp.ui.features.conversations.detail.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.domain.models.Message
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun rememberLazyListStateWithAutoscroll(messages: List<Message>): LazyListState {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    return listState
}

@DevicePreviews
@Preview
@Composable
private fun RememberLazyListStatePreview() {
    LiveChatPreviewContainer {
        val messages = PreviewFixtures.sampleMessages
        val listState = rememberLazyListStateWithAutoscroll(messages)
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(messages, key = Message::id) { message ->
                androidx.compose.material3.Text(text = message.body)
            }
        }
    }
}
