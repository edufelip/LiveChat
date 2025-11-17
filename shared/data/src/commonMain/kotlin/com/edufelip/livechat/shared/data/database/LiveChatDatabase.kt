package com.edufelip.livechat.shared.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ContactEntity::class,
        MessageEntity::class,
        ConversationStateEntity::class,
        OnboardingStatusEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class LiveChatDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun conversationStateDao(): ConversationStateDao
    abstract fun onboardingStatusDao(): OnboardingStatusDao
}

fun RoomDatabase.Builder<LiveChatDatabase>.configureDefaults(): RoomDatabase.Builder<LiveChatDatabase> =
    this.setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration()

fun buildLiveChatDatabase(builder: RoomDatabase.Builder<LiveChatDatabase>): LiveChatDatabase =
    builder.configureDefaults().build()

const val LIVE_CHAT_DB_NAME = "livechat.db"
