package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.session.FirebaseUserSessionProvider
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AndroidSessionBridge(
    firebaseUserSessionProvider: FirebaseUserSessionProvider,
    private val inMemoryUserSessionProvider: InMemoryUserSessionProvider,
    dispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        scope.launch {
            firebaseUserSessionProvider.session.collectLatest { session ->
                inMemoryUserSessionProvider.setSession(session)
            }
        }
    }
}
