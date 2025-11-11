package com.edufelip.livechat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import com.edufelip.livechat.contacts.AndroidContactsProvider
import com.edufelip.livechat.ui.app.LiveChatApp
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            LiveChatApp(
                phoneContactsProvider = {
                    AndroidContactsProvider.fetch(applicationContext)
                },
                onShareInvite = { request ->
                    shareInvite(request)
                },
                onOpenSettingsSection = { request ->
                    showSettingsSection(request)
                },
            )
        }
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

    private fun showSettingsSection(request: SettingsNavigationRequest) {
        Toast.makeText(this, request.placeholderMessage, Toast.LENGTH_SHORT).show()
    }
}
