package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InboxActionsDao {
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM processed_inbox_actions
            WHERE action_id = :actionId
        )
        """,
    )
    suspend fun hasAction(actionId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(action: ProcessedInboxActionEntity)
}
