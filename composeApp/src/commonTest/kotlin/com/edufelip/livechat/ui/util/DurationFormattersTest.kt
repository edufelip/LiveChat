package com.edufelip.livechat.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormattersTest {
    @Test
    fun formatDurationMillisPadsMinutesAndSeconds() {
        assertEquals("00:00", formatDurationMillis(0))
        assertEquals("00:01", formatDurationMillis(1_000))
        assertEquals("01:01", formatDurationMillis(61_000))
    }

    @Test
    fun formatDurationMillisClampsNegativeValues() {
        assertEquals("00:00", formatDurationMillis(-1_000))
    }
}
