package com.edufelip.livechat.shared.data

import androidx.room.Room
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.buildLiveChatDatabase
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

fun createIosTestDatabase(): LiveChatDatabase {
    val path = NSTemporaryDirectory() + "/livechat-test-" + NSUUID().UUIDString + ".db"
    return buildLiveChatDatabase(
        Room.databaseBuilder<LiveChatDatabase>(
            name = path,
        ),
    )
}
