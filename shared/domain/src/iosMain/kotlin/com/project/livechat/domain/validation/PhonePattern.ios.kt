package com.project.livechat.domain.validation

import kotlin.text.Regex

private val phoneRegex = Regex("^\\+?[0-9 .()-]{7,20}")

internal actual fun isPhoneNumberValid(raw: String): Boolean {
    return phoneRegex.matches(raw)
}
