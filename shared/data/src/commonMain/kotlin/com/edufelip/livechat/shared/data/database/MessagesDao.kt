package com.edufelip.livechat.shared.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Query(
        """
        SELECT * FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at
        """,
    )
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at
        """,
    )
    suspend fun getMessages(conversationId: String): List<MessageEntity>

    @Query(
        """
        DELETE FROM messages
        WHERE id = :messageId
           OR local_temp_id = :messageId
        """,
    )
    suspend fun deleteMessage(messageId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query(
        """
        UPDATE messages
        SET body = :body,
            metadata = :metadata
        WHERE id = :messageId
        """,
    )
    suspend fun updateBodyAndMetadata(
        messageId: String,
        body: String,
        metadata: String?,
    )

    @Query("UPDATE messages SET metadata = :metadata WHERE id = :messageId")
    suspend fun updateMetadata(
        messageId: String,
        metadata: String?,
    )

    @Query(
        """
        UPDATE messages
        SET id = :serverId,
            status = :status,
            local_temp_id = NULL
        WHERE local_temp_id = :localId
        """,
    )
    suspend fun updateStatusByLocalId(
        localId: String,
        serverId: String,
        status: String,
    )

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(
        messageId: String,
        status: String,
    )

    @Query(
        """
        UPDATE messages
        SET status = :nextStatus
        WHERE status = :currentStatus
        """,
    )
    suspend fun updateStatusByValue(
        currentStatus: String,
        nextStatus: String,
    )

    @Query("SELECT status FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getStatus(messageId: String): String?

    @Query(
        """
        SELECT created_at FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at DESC
        LIMIT 1
        """,
    )
    suspend fun latestTimestamp(conversationId: String): Long?

    @Query(
        """
        SELECT * FROM messages
        WHERE conversation_id = :conversationId
          AND sender_id != :currentUserId
        ORDER BY created_at DESC
        LIMIT 1
        """,
    )
    suspend fun latestIncomingMessage(
        conversationId: String,
        currentUserId: String,
    ): MessageEntity?

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun clearConversation(conversationId: String)

    @Query(
        """
        DELETE FROM messages
        WHERE conversation_id = :conversationId
          AND id NOT IN (:messageIds)
        """,
    )
    suspend fun deleteMessagesOutsideConversation(
        conversationId: String,
        messageIds: List<String>,
    )

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM messages
            WHERE conversation_id = :conversationId AND id = :messageId
        )
        """,
    )
    suspend fun hasMessage(
        conversationId: String,
        messageId: String,
    ): Boolean

    @Query(
        """
        WITH latest AS (
            SELECT conversation_id, MAX(created_at) AS max_created_at
            FROM messages
            GROUP BY conversation_id
        )
        SELECT
            m.conversation_id AS conversationId,
            m.id AS messageId,
            m.sender_id AS senderId,
            m.body AS body,
            m.content_type AS contentType,
            m.created_at AS createdAt,
            m.status AS status,
            cs.last_read_at AS lastReadAt,
            cs.is_pinned AS isPinned,
            cs.pinned_at AS pinnedAt,
            cs.mute_until AS muteUntil,
            cs.archived AS archived,
            c.name AS contactName,
            c.photo AS contactPhoto,
            c.firebase_uid AS contactFirebaseUid,
            (
                SELECT COUNT(*)
                FROM messages AS unread
                WHERE unread.conversation_id = m.conversation_id
                  AND unread.created_at > IFNULL(cs.last_read_at, 0)
            ) AS unreadCount
        FROM latest
        JOIN messages m ON m.conversation_id = latest.conversation_id AND m.created_at = latest.max_created_at
        LEFT JOIN conversation_state cs ON cs.conversation_id = m.conversation_id
        LEFT JOIN contacts c ON (c.phone_no = m.conversation_id OR c.firebase_uid = m.conversation_id)
        ORDER BY
            CASE WHEN IFNULL(cs.is_pinned, 0) = 1 THEN 0 ELSE 1 END,
            m.created_at DESC
        """,
    )
    fun observeConversationSummaries(): Flow<List<ConversationSummaryRow>>
}

data class ConversationSummaryRow(
    val conversationId: String,
    val messageId: String,
    val senderId: String,
    val body: String,
    val contentType: String?,
    val createdAt: Long,
    val status: String,
    val lastReadAt: Long?,
    val isPinned: Boolean?,
    val pinnedAt: Long?,
    val muteUntil: Long?,
    val archived: Boolean?,
    val contactName: String?,
    val contactPhoto: String?,
    val contactFirebaseUid: String?,
    val unreadCount: Long?,
)
