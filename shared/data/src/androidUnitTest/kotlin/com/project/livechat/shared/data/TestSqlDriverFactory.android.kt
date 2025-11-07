package com.project.livechat.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.project.livechat.shared.data.database.LiveChatDatabase

actual fun createTestSqlDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    LiveChatDatabase.Schema.create(driver)
    return driver
}
