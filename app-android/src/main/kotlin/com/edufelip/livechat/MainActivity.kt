package com.edufelip.livechat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.edufelip.livechat.contacts.AndroidContactsProvider
import com.edufelip.livechat.notifications.NotificationIntentKeys
import com.edufelip.livechat.notifications.NotificationNavigation
import com.edufelip.livechat.notifications.NotificationNavigationTarget
import com.edufelip.livechat.notifications.PlatformTokenRegistrar
import com.edufelip.livechat.ui.app.LiveChatApp
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize platform token registrar
        PlatformTokenRegistrar.initialize(applicationContext)

        enableEdgeToEdge()
        // Ensure system bars are transparent so content can draw behind them.
        @Suppress("DEPRECATION")
        run {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            // Keep icons legible by following the current theme background.
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            LiveChatApp(
                phoneContactsProvider = {
                    AndroidContactsProvider.fetch(applicationContext)
                },
                onShareInvite = { request ->
                    shareInvite(request)
                },
            )
        }
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun shareInvite(request: InviteShareRequest) {
        val genericShare =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, request.message)
            }
        val chooserTitle = request.chooserTitle

        runCatching {
            startActivity(Intent.createChooser(genericShare, chooserTitle))
        }.onFailure {
            Toast.makeText(this, request.unavailableMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val conversationId = intent?.getStringExtra(NotificationIntentKeys.EXTRA_CONVERSATION_ID) ?: return
        val senderName = intent.getStringExtra(NotificationIntentKeys.EXTRA_SENDER_NAME)
        NotificationNavigation.emit(NotificationNavigationTarget(conversationId, senderName))
    }
}
