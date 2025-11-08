package com.edufelip.livechat.composeapp.ui.util

fun String.isDigitsOnly(): Boolean = isNotEmpty() && all(Char::isDigit)
