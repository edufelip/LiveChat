package com.edufelip.livechat.domain.utils

import com.edufelip.livechat.domain.models.ConversationSummary

object ConversationListSnapshotCache {
    @Volatile
    private var cached: List<ConversationSummary>? = null

    fun snapshot(): List<ConversationSummary>? = cached

    fun seed(items: List<ConversationSummary>) {
        if (cached != null) return
        cached = items
    }

    fun update(items: List<ConversationSummary>) {
        cached = items
    }

    fun clear() {
        cached = null
    }
}
