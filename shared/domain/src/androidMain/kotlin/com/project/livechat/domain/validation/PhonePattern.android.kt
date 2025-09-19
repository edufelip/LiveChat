package com.project.livechat.domain.validation

import android.util.Patterns

internal actual fun isPhoneNumberValid(raw: String): Boolean {
    return Patterns.PHONE.matcher(raw).matches()
}
