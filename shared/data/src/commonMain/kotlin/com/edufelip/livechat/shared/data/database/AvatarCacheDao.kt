package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AvatarCacheDao {
    @Query("SELECT * FROM avatar_cache WHERE owner_id = :ownerId LIMIT 1")
    suspend fun get(ownerId: String): AvatarCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AvatarCacheEntity)

    @Query("DELETE FROM avatar_cache WHERE owner_id = :ownerId")
    suspend fun delete(ownerId: String)

    @Query("DELETE FROM avatar_cache")
    suspend fun deleteAll()
}
