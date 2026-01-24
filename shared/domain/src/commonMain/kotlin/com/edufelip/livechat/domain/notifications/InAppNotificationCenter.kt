package com.edufelip.livechat.domain.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotification(
    val title: String,
    val body: String,
    val conversationId: String? = null,
    val messageId: String? = null,
)

object InAppNotificationCenter {
    private val _events = MutableSharedFlow<InAppNotification>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emit(notification: InAppNotification) {
        _events.tryEmit(notification)
    }
}
