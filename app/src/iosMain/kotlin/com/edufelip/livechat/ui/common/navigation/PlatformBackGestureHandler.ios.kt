package com.edufelip.livechat.ui.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIGestureRecognizerStateRecognized
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject

private const val EDGE_BACK_TRIGGER_DISTANCE = 40.0

@Composable
actual fun PlatformBackGestureHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    if (!enabled) return
    val onBackState = rememberUpdatedState(onBack)

    DisposableEffect(Unit) {
        val rootViewController = currentRootViewController()
        val rootView = rootViewController?.view ?: return@DisposableEffect onDispose {}

        val handler =
            EdgePanHandler(
                onBack = { onBackState.value.invoke() },
            )

        @OptIn(ExperimentalForeignApi::class)
        val recognizer =
            UIScreenEdgePanGestureRecognizer(
                target = handler,
                action = NSSelectorFromString("handleEdgePan:"),
            ).apply {
                edges = UIRectEdgeLeft
                cancelsTouchesInView = false
                delaysTouchesBegan = false
                delaysTouchesEnded = false
                delegate = handler
                maximumNumberOfTouches = 1u
            }

        rootView.addGestureRecognizer(recognizer)

        onDispose {
            rootView.removeGestureRecognizer(recognizer)
        }
    }
}

private class EdgePanHandler(
    private val onBack: () -> Unit,
) : NSObject(),
    UIGestureRecognizerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun handleEdgePan(recognizer: UIScreenEdgePanGestureRecognizer) {
        when (recognizer.state) {
            UIGestureRecognizerStateEnded,
            UIGestureRecognizerStateRecognized,
            -> {
                val translation = recognizer.translationInView(recognizer.view)
                val x = translation.useContents { x }
                if (x >= EDGE_BACK_TRIGGER_DISTANCE) {
                    onBack()
                }
            }
            else -> Unit
        }
    }

    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer,
    ): Boolean = true
}

private fun currentRootViewController(): UIViewController? {
    val windows = UIApplication.sharedApplication.windows as? List<*>
    val keyWindow = windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
    return (keyWindow ?: windows?.firstOrNull() as? UIWindow)?.rootViewController
}
