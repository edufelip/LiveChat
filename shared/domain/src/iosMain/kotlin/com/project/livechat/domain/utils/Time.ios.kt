package com.project.livechat.domain.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

@OptIn(ExperimentalForeignApi::class)
actual fun currentEpochMillis(): Long =
    memScoped {
        val timeVal = alloc<timeval>()
        gettimeofday(timeVal.ptr, null)
        timeVal.tv_sec.toLong() * 1000L + timeVal.tv_usec.toLong() / 1000L
    }
