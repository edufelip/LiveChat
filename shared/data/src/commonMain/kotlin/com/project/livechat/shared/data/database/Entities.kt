package com.project.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "phone_no")
    val phoneNo: String,
    val description: String?,
    val photo: String?,
    @ColumnInfo(name = "is_registered")
    val isRegistered: Boolean,
)

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

@Entity(tableName = "conversation_state")
data class ConversationStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val role: String = "Member",
    @ColumnInfo(name = "joined_at")
    val joinedAt: Long,
    @ColumnInfo(name = "left_at")
    val leftAt: Long?,
    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long = 0,
    @ColumnInfo(name = "last_read_seq")
    val lastReadSeq: Long?,
    @ColumnInfo(name = "mute_until")
    val muteUntil: Long?,
    val archived: Boolean = false,
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo(name = "pinned_at")
    val pinnedAt: Long?,
    val settings: String?,
)

@Entity(tableName = "onboarding_status")
data class OnboardingStatusEntity(
    @PrimaryKey
    val id: Int = 0,
    val complete: Boolean,
)
