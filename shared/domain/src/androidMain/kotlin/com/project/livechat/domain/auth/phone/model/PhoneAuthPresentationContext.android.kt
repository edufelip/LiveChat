package com.project.livechat.domain.auth.phone.model

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

actual class PhoneAuthPresentationContext internal constructor(val activity: Activity)

actual fun phoneAuthPresentationContext(platformContext: Any?): PhoneAuthPresentationContext {
    val activity = when (platformContext) {
        is Activity -> platformContext
        is Context -> platformContext.findActivity()
        else -> null
    } ?: error("PhoneAuthPresentationContext requires an Activity")
    return PhoneAuthPresentationContext(activity)
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
