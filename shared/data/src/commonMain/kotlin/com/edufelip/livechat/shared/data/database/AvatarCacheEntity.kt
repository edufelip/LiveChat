package com.edufelip.livechat.shared.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avatar_cache")
data class AvatarCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "remote_url")
    val remoteUrl: String,
    @ColumnInfo(name = "local_path")
    val localPath: String,
    @ColumnInfo(name = "updated_at")
    val updatedAtMillis: Long,
)
