package com.edufelip.livechat.shared.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AvatarCacheEntity::class,
        ContactEntity::class,
        MessageEntity::class,
        ConversationStateEntity::class,
        ProcessedInboxActionEntity::class,
        OnboardingStatusEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@ConstructedBy(LiveChatDatabaseConstructor::class)
abstract class LiveChatDatabase : RoomDatabase() {
    abstract fun avatarCacheDao(): AvatarCacheDao

    abstract fun contactsDao(): ContactsDao

    abstract fun messagesDao(): MessagesDao

    abstract fun conversationStateDao(): ConversationStateDao

    abstract fun inboxActionsDao(): InboxActionsDao

    abstract fun onboardingStatusDao(): OnboardingStatusDao
}

expect object LiveChatDatabaseConstructor : RoomDatabaseConstructor<LiveChatDatabase>

fun RoomDatabase.Builder<LiveChatDatabase>.configureDefaults(): RoomDatabase.Builder<LiveChatDatabase> =
    this
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration(dropAllTables = false)

fun buildLiveChatDatabase(builder: RoomDatabase.Builder<LiveChatDatabase>): LiveChatDatabase = builder.configureDefaults().build()

const val LIVE_CHAT_DB_NAME = "livechat.db"

private val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE onboarding_status ADD COLUMN welcome_seen INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

private val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS avatar_cache (
                    owner_id TEXT NOT NULL,
                    remote_url TEXT NOT NULL,
                    local_path TEXT NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY(owner_id)
                )
                """.trimIndent(),
            )
        }
    }
