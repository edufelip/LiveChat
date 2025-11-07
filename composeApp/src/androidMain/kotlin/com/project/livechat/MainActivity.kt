package com.project.livechat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.project.livechat.composeapp.contacts.AndroidContactsProvider
import com.project.livechat.composeapp.ui.app.LiveChatApp
import com.project.livechat.composeapp.ui.features.contacts.model.InviteShareRequest
import com.project.livechat.composeapp.ui.features.settings.screens.SettingsSection
import com.project.livechat.composeapp.ui.features.settings.screens.title
import com.project.livechat.composeapp.ui.resources.LiveChatStrings
import com.project.livechat.domain.models.InviteChannel

class MainActivity : ComponentActivity() {
    private val sharedStrings = LiveChatStrings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEdgeToEdgeAppearance()

        setContent {
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
        val contact = request.contact
        val phoneNumber = contact.phoneNo.takeIf { it.isNotBlank() }
        val emailAddress = contact.description?.takeIf { it.isValidEmail() }
        val genericShare =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, request.message)
            }
        val chooserTitle = "Invite via ${request.channel.displayName}"

        val targetedIntent =
            when (request.channel) {
                InviteChannel.Sms ->
                    Intent(Intent.ACTION_SENDTO).apply {
                        val smsUri = phoneNumber?.let { "smsto:${Uri.encode(it)}" } ?: "smsto:"
                        data = Uri.parse(smsUri)
                        putExtra("sms_body", request.message)
                        phoneNumber?.let { putExtra("address", it) }
                    }

                InviteChannel.Email ->
                    Intent(Intent.ACTION_SENDTO).apply {
                        val encoded = emailAddress?.let { Uri.encode(it) } ?: ""
                        data = Uri.parse("mailto:$encoded")
                        emailAddress?.let { putExtra(Intent.EXTRA_EMAIL, arrayOf(it)) }
                        putExtra(Intent.EXTRA_SUBJECT, "Join me on LiveChat")
                        putExtra(Intent.EXTRA_TEXT, request.message)
                    }

                InviteChannel.WhatsApp ->
                    contact.phoneNo.toWhatsAppDeepLink(request.message)?.let { uri ->
                        Intent(Intent.ACTION_VIEW).apply {
                            data = uri
                            setPackage("com.whatsapp")
                        }
                    }

                InviteChannel.Share -> null
            }

        val intent =
            targetedIntent?.takeIf { it.resolveActivity(packageManager) != null }
                ?: genericShare

        val shouldUseChooser = targetedIntent == null || intent === genericShare
        val activityIntent =
            if (shouldUseChooser) {
                Intent.createChooser(intent, chooserTitle)
            } else {
                intent
            }

        runCatching {
            startActivity(activityIntent)
        }.onFailure {
            startActivity(Intent.createChooser(genericShare, chooserTitle))
        }
    }

    private fun showSettingsSection(section: SettingsSection) {
        val message = "Opening ${section.title(sharedStrings.settings)} settings soon"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setEdgeToEdgeAppearance() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val isDarkTheme =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        controller.isAppearanceLightStatusBars = !isDarkTheme
        controller.isAppearanceLightNavigationBars = !isDarkTheme
    }
}

private fun String.isValidEmail(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

private fun String.toWhatsAppDeepLink(message: String): Uri? {
    val normalized = filter { it.isDigit() }
    if (normalized.isEmpty()) return null
    val encodedMessage = Uri.encode(message)
    return Uri.parse("https://wa.me/$normalized?text=$encodedMessage")
}
