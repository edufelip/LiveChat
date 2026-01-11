package com.edufelip.livechat.domain.utils

import platform.Foundation.NSThread

internal actual fun isMainThread(): Boolean = NSThread.isMainThread
