package com.edufelip.livechat

import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.livechat.data.bridge.IosBridgeBundle
import com.edufelip.livechat.data.di.IosKoinBridge
import com.edufelip.livechat.data.di.startKoinForiOS
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.di.presentationModule
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.ui.app.LiveChatApp
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIRectEdgeAll
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

@Suppress("ktlint:standard:function-naming")
fun MainViewController(
    config: FirebaseRestConfig = defaultFirebaseConfig(),
    bridgeBundle: IosBridgeBundle,
    userId: String = "",
    idToken: String? = null,
    phoneNumber: String? = null,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
): UIViewController {
    LiveChatIosInitializer.ensure(config, bridgeBundle)
    LiveChatIosInitializer.updateSession(userId, idToken, phoneNumber)
    return ComposeUIViewController {
        LiveChatApp(
            phoneContactsProvider = phoneContactsProvider,
            onShareInvite = { request ->
                handleInviteShare(request)
            },
        )
    }.apply {
        edgesForExtendedLayout = UIRectEdgeAll
        extendedLayoutIncludesOpaqueBars = true
    }
}

fun updateLiveChatSession(
    userId: String,
    idToken: String? = null,
    phoneNumber: String? = null,
) {
    LiveChatIosInitializer.updateSession(userId, idToken, phoneNumber)
}

private object LiveChatIosInitializer {
    private var started = false

    fun ensure(
        config: FirebaseRestConfig,
        bridgeBundle: IosBridgeBundle,
    ) {
        if (!started) {
            startKoinForiOS(config, bridgeBundle, extraModules = listOf(presentationModule))
            started = true
        }
    }

    fun updateSession(
        userId: String,
        idToken: String?,
        phoneNumber: String?,
    ) {
        val provider = IosKoinBridge.sessionProvider()
        if (userId.isBlank()) {
            provider.setSession(null)
            return
        }
        provider.setSession(
            UserSession(
                userId = userId,
                idToken = idToken.orEmpty(),
                phoneNumber = phoneNumber,
            ),
        )
    }
}

fun defaultFirebaseConfig() =
    run {
        val emulator = iosEmulatorOverrides()
        FirebaseRestConfig(
            projectId = "YOUR_FIREBASE_PROJECT",
            apiKey = "YOUR_FIREBASE_API_KEY",
            emulatorHost = emulator?.first,
            emulatorPort = emulator?.second,
            usersCollection = "users",
            messagesCollection = "items",
            conversationsCollection = "inboxes",
            invitesCollection = "invites",
            websocketEndpoint = "",
            pollingIntervalMs = 5_000,
            defaultRegionIso = null,
        )
    }

private fun iosEmulatorOverrides(): Pair<String, Int>? {
    val environment = platform.Foundation.NSProcessInfo.processInfo.environment
    val enabled =
        environment["FIREBASE_EMULATOR_ENABLED"]?.toString()?.lowercase() in listOf("1", "true", "yes")
    if (!enabled) return null
    val host = environment["FIREBASE_EMULATOR_HOST"]?.toString()?.ifBlank { null } ?: "127.0.0.1"
    val port =
        environment["FIREBASE_FIRESTORE_EMULATOR_PORT"]?.toString()?.toIntOrNull()
            ?: 8080
    return host to port
}

private fun presentShareSheet(message: String) {
    val controller = topViewController() ?: return
    val activityController = UIActivityViewController(activityItems = listOf(message), applicationActivities = null)
    controller.presentViewController(activityController, animated = true, completion = null)
}

private fun handleInviteShare(request: InviteShareRequest) {
    presentShareSheet(request.message)
}

private fun topViewController(root: UIViewController? = currentRootViewController()): UIViewController? {
    val presented = root?.presentedViewController ?: return root
    return when (presented) {
        is UINavigationController -> topViewController(presented.visibleViewController)
        else -> topViewController(presented)
    }
}

private fun currentRootViewController(): UIViewController? {
    val windows = UIApplication.sharedApplication.windows as? List<*>
    val keyWindow = windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
    return (keyWindow ?: windows?.firstOrNull() as? UIWindow)?.rootViewController
}
