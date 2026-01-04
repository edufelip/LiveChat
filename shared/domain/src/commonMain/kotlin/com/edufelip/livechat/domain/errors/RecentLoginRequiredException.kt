package com.edufelip.livechat.domain.errors

class RecentLoginRequiredException(
    message: String = "Recent login required",
) : RuntimeException(message)
