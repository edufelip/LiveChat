package com.edufelip.livechat.ui.util

fun String.isDigitsOnly(): Boolean = isNotEmpty() && all(Char::isDigit)
