package com.edufelip.livechat.domain.utils

import kotlinx.coroutines.flow.MutableStateFlow

object ContactsSyncSession {
    private val appOpenToken = MutableStateFlow(0L)
    private val lastSyncedToken = MutableStateFlow(-1L)

    fun markAppOpen() {
        appOpenToken.value = appOpenToken.value + 1
    }

    fun canSync(): Boolean = appOpenToken.value > lastSyncedToken.value

    fun markSynced() {
        lastSyncedToken.value = appOpenToken.value
    }

    fun reset() {
        appOpenToken.value = 0L
        lastSyncedToken.value = -1L
    }
}
