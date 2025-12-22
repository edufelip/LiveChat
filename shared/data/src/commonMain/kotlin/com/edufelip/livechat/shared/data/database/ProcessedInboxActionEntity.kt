package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_inbox_actions")
data class ProcessedInboxActionEntity(
    @PrimaryKey
    @ColumnInfo(name = "action_id")
    val actionId: String,
    @ColumnInfo(name = "processed_at")
    val processedAt: Long,
)
