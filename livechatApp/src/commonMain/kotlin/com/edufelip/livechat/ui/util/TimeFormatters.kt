package com.edufelip.livechat.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Long.formatAsTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    runCatching {
        val instant = Instant.fromEpochMilliseconds(this)
        val local = instant.toLocalDateTime(timeZone)
        "${local.hour.pad2()}:${local.minute.pad2()}"
    }.getOrDefault("")

private fun Int.pad2(): String = toString().padStart(2, '0')
