package com.edufelip.livechat.ui.features.settings.account

import com.edufelip.livechat.data.di.IosKoinBridge

actual fun signOutPlatformUser() {
    IosKoinBridge.authBridge().signOut()
}
