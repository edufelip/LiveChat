package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
