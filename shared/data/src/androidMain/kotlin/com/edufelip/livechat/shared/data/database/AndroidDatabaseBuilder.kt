package com.edufelip.livechat.shared.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun createAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<LiveChatDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(LIVE_CHAT_DB_NAME)
    return Room.databaseBuilder<LiveChatDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
