package com.edufelip.livechat.ui.util

internal fun formatDurationMillis(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val minutesText = minutes.toString().padStart(2, '0')
    val secondsText = seconds.toString().padStart(2, '0')
    return "$minutesText:$secondsText"
}
