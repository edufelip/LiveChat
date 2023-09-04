package com.project.livechat.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

inline fun CoroutineScope.launchLatchTest(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    latch: CountDownLatch,
    crossinline exec: suspend () -> Unit,
) {
    val job = launch(dispatcher) {
        exec()
        latch.countDown()
    }

    latch.await(5, TimeUnit.SECONDS)
    job.cancel()
}