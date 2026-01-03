package com.edufelip.livechat.ui.features.settings.account

import com.google.firebase.auth.FirebaseAuth

actual fun signOutPlatformUser() {
    FirebaseAuth.getInstance().signOut()
}
