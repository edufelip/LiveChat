package com.project.livechat.composeapp

import com.project.livechat.di.AndroidKoinBridge
import com.project.livechat.domain.providers.model.UserSession

@Suppress("unused")
object LiveChatAndroidInitializer {
    fun updateSession(
        userId: String,
        idToken: String? = null,
    ) {
        val provider = AndroidKoinBridge.sessionProvider()
        provider.setSession(
            UserSession(
                userId = userId,
                idToken = idToken.orEmpty(),
            ),
        )
    }
}
