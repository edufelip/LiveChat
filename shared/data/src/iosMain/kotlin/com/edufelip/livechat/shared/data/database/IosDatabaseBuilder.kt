@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.shared.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

fun createIosDatabaseBuilder(): RoomDatabase.Builder<LiveChatDatabase> {
    val path = documentDirectory() + "/" + LIVE_CHAT_DB_NAME
    return Room.databaseBuilder<LiveChatDatabase>(name = path)
}

private fun documentDirectory(): String {
    val manager = NSFileManager.defaultManager
    val url: NSURL? = manager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "Unable to resolve iOS documents directory" }
}
