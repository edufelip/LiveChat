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
import com.edufelip.livechat.ui.features.settings.screens.SettingsSection
import com.edufelip.livechat.ui.features.settings.screens.title
import com.edufelip.livechat.ui.resources.LiveChatStrings

class MainActivity : ComponentActivity() {
    private val sharedStrings = LiveChatStrings()
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
                onOpenSettingsSection = { section ->
                    showSettingsSection(section)
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
        val chooserTitle = sharedStrings.contacts.inviteShareTitle

        runCatching {
            startActivity(Intent.createChooser(genericShare, chooserTitle))
        }.onFailure {
            Toast.makeText(this, "No apps available to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSettingsSection(section: SettingsSection) {
        val message = "Opening ${section.title(sharedStrings.settings)} settings soon"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
