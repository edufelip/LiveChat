package com.edufelip.livechat.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationNavigationTarget(
    val conversationId: String,
    val senderName: String? = null,
)

object NotificationNavigation {
    private val _events = MutableSharedFlow<NotificationNavigationTarget>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emit(target: NotificationNavigationTarget) {
        _events.tryEmit(target)
    }
}
