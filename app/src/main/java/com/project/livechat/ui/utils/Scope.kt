package com.project.livechat.ui.utils

fun <T : Any> coalesce(vararg elements: T?): List<T>? {
    return if (elements.any { it == null }) null else elements.filterNotNull()
}