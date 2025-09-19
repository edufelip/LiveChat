package com.project.livechat.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.project.livechat.shared.data.database.LiveChatDatabase
import kotlin.random.Random

actual fun createTestSqlDriver(): SqlDriver {
    val dbName = "test-${Random.nextInt()}.db"
    return NativeSqliteDriver(LiveChatDatabase.Schema, name = dbName)
}
