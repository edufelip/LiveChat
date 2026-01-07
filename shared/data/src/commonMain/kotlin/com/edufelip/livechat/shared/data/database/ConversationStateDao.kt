package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationStateDao {
    @Query("SELECT * FROM conversation_state WHERE conversation_id = :conversationId")
    fun observeConversationState(conversationId: String): Flow<ConversationStateEntity?>

    @Query("SELECT * FROM conversation_state WHERE conversation_id = :conversationId")
    suspend fun getConversationState(conversationId: String): ConversationStateEntity?

    @Query("DELETE FROM conversation_state WHERE conversation_id = :conversationId")
    suspend fun deleteConversationState(conversationId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ConversationStateEntity)
}
