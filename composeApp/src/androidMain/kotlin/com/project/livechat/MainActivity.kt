package com.project.livechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.livechat.composeapp.contacts.AndroidContactsProvider
import com.project.livechat.composeapp.ui.app.LiveChatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LiveChatApp(
                phoneContactsProvider = {
                    AndroidContactsProvider.fetch(applicationContext)
                },
            )
        }
    }
}
