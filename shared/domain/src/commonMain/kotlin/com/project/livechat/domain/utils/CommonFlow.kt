package com.project.livechat.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Closeable(private val onClose: () -> Unit) {
    fun close() = onClose()
}

class CFlow<T>(private val origin: Flow<T>) {
    fun watch(block: (T) -> Unit): Closeable {
        val scope = MainScope()
        val job = scope.launch {
            origin.collect { value ->
                block(value)
            }
        }
        return Closeable {
            job.cancel()
            scope.cancel()
        }
    }
}

class CStateFlow<T>(private val origin: StateFlow<T>) {
    val value: T
        get() = origin.value

    fun watch(block: (T) -> Unit): Closeable {
        val scope: CoroutineScope = MainScope()
        val job = scope.launch {
            origin.collect { value ->
                block(value)
            }
        }
        return Closeable {
            job.cancel()
            scope.cancel()
        }
    }
}

fun <T> Flow<T>.asCFlow(): CFlow<T> = CFlow(this)

fun <T> StateFlow<T>.asCStateFlow(): CStateFlow<T> = CStateFlow(this)
