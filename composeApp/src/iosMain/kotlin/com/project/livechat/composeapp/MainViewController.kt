package com.project.livechat.composeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.project.livechat.composeapp.ui.app.LiveChatApp
import com.project.livechat.data.di.IosKoinBridge
import com.project.livechat.data.di.startKoinForiOS
import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.providers.model.UserSession
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(
    config: FirebaseRestConfig = defaultFirebaseConfig(),
    userId: String = "demo-user",
    idToken: String? = null,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
): UIViewController {
    LiveChatIosInitializer.ensure(config)
    LiveChatIosInitializer.updateSession(userId, idToken)
    return ComposeUIViewController {
        LiveChatApp(phoneContactsProvider = phoneContactsProvider)
    }
}

fun updateLiveChatSession(
    userId: String,
    idToken: String? = null,
) {
    LiveChatIosInitializer.updateSession(userId, idToken)
}

private object LiveChatIosInitializer {
    private var started = false

    fun ensure(config: FirebaseRestConfig) {
        if (!started) {
            startKoinForiOS(config)
            started = true
        }
    }

    fun updateSession(
        userId: String,
        idToken: String?,
    ) {
        val provider = IosKoinBridge.sessionProvider()
        provider.setSession(
            UserSession(
                userId = userId,
                idToken = idToken.orEmpty(),
            ),
        )
    }
}

fun defaultFirebaseConfig() =
    FirebaseRestConfig(
        projectId = "YOUR_FIREBASE_PROJECT",
        apiKey = "YOUR_FIREBASE_API_KEY",
        usersCollection = "users",
        messagesCollection = "messages",
        conversationsCollection = "conversations",
        invitesCollection = "invites",
        websocketEndpoint = "",
        pollingIntervalMs = 5_000,
    )
