package com.edufelip.livechat.shared.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        ConversationStateEntity::class,
        ProcessedInboxActionEntity::class,
        OnboardingStatusEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(LiveChatDatabaseConstructor::class)
abstract class LiveChatDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao

    abstract fun messagesDao(): MessagesDao

    abstract fun conversationStateDao(): ConversationStateDao

    abstract fun inboxActionsDao(): InboxActionsDao

    abstract fun onboardingStatusDao(): OnboardingStatusDao
}

expect object LiveChatDatabaseConstructor : RoomDatabaseConstructor<LiveChatDatabase>

fun RoomDatabase.Builder<LiveChatDatabase>.configureDefaults(): RoomDatabase.Builder<LiveChatDatabase> =
    this.setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration(dropAllTables = false)

fun buildLiveChatDatabase(builder: RoomDatabase.Builder<LiveChatDatabase>): LiveChatDatabase = builder.configureDefaults().build()

const val LIVE_CHAT_DB_NAME = "livechat.db"
