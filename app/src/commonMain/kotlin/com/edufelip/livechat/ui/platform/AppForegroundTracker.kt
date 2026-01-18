package com.edufelip.livechat.ui.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppForegroundTracker {
    private val _isForeground = MutableStateFlow(false)
    val isForeground = _isForeground.asStateFlow()

    fun onForeground() {
        _isForeground.value = true
    }

    fun onBackground() {
        _isForeground.value = false
    }
}
