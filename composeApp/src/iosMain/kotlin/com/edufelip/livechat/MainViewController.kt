package com.edufelip.livechat

import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.livechat.ui.app.LiveChatApp
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.data.di.IosKoinBridge
import com.edufelip.livechat.data.di.startKoinForiOS
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.providers.model.UserSession
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.keyWindow

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
        LiveChatApp(
            phoneContactsProvider = phoneContactsProvider,
            onShareInvite = { request ->
                handleInviteShare(request)
            },
            onOpenSettingsSection = { request ->
                presentSettingsSection(request)
            },
        )
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
        defaultRegionIso = NSLocale.currentLocale.countryCode,
    )

private fun presentShareSheet(message: String) {
    val controller = topViewController() ?: return
    val activityController = UIActivityViewController(activityItems = listOf(message), applicationActivities = null)
    controller.presentViewController(activityController, animated = true, completion = null)
}

private fun presentSettingsSection(request: SettingsNavigationRequest) {
    val controller = topViewController() ?: return
    val alert =
        UIAlertController(
            title = request.title,
            message = request.description,
            preferredStyle = UIAlertControllerStyleAlert,
        )
    alert.addAction(
        UIAlertAction.actionWithTitle(
            title = "Close",
            style = UIAlertActionStyleDefault,
            handler = null,
        ),
    )
    controller.presentViewController(alert, animated = true, completion = null)
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
    UIApplication.sharedApplication.keyWindow?.rootViewController?.let { return it }
    val windows = UIApplication.sharedApplication.windows as? List<*>
    val keyWindow = windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow == true } as? UIWindow
    return (keyWindow ?: windows?.firstOrNull() as? UIWindow)?.rootViewController
}
