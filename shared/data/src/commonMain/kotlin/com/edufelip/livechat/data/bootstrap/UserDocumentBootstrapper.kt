package com.edufelip.livechat.data.bootstrap

import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.domain.providers.UserSessionProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserDocumentBootstrapper(
    sessionProvider: UserSessionProvider,
    private val accountRemoteData: IAccountRemoteData,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastUserId: String? = null

    init {
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                val userId = session?.userId?.takeIf { it.isNotBlank() }
                val token = session?.idToken?.takeIf { it.isNotBlank() }
                if (userId == null || token == null) {
                    lastUserId = null
                    return@collectLatest
                }
                if (lastUserId == userId) return@collectLatest
                lastUserId = userId
                runCatching {
                    accountRemoteData.ensureUserDocument(
                        userId = userId,
                        idToken = token,
                        phoneNumber = session.phoneNumber,
                    )
                }
            }
        }
    }
}
