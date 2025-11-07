package com.project.livechat.shared.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.project.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.Dispatchers
import java.io.File

fun createTestDatabase(): LiveChatDatabase {
    val context: Context = ApplicationProvider.getApplicationContext()
    val dbFile = File.createTempFile("livechat-test", ".db")
    return Room.databaseBuilder<LiveChatDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration()
        .build()
}
