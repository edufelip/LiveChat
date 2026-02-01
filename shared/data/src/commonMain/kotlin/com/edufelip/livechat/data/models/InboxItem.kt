package com.edufelip.livechat.data.models

import com.edufelip.livechat.domain.models.Message

enum class InboxActionType {
    DELIVERED,
    READ,
}

data class InboxAction(
    val id: String,
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val actionType: InboxActionType,
    val actionAtMillis: Long,
)

sealed class InboxItem {
    data class MessageItem(
        val message: Message,
    ) : InboxItem()

    data class ActionItem(
        val action: InboxAction,
    ) : InboxItem()
}
