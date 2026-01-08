package com.edufelip.livechat

import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.providers.model.UserSession

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
