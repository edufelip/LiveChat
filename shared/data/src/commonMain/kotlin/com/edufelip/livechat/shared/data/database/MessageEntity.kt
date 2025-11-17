package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index("conversation_id"), Index("reply_to_message_id"), Index("thread_root_id")],
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    val body: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val status: String,
    @ColumnInfo(name = "local_temp_id")
    val localTempId: String?,
    @ColumnInfo(name = "message_seq")
    val messageSeq: Long?,
    @ColumnInfo(name = "server_ack_at")
    val serverAckAt: Long?,
    @ColumnInfo(name = "content_type")
    val contentType: String?,
    val ciphertext: String?,
    val attachments: String?,
    @ColumnInfo(name = "reply_to_message_id")
    val replyToMessageId: String?,
    @ColumnInfo(name = "thread_root_id")
    val threadRootId: String?,
    @ColumnInfo(name = "edited_at")
    val editedAt: Long?,
    @ColumnInfo(name = "deleted_for_all_at")
    val deletedForAllAt: Long?,
    val metadata: String?,
)
