package com.project.livechat.data.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.providers.model.UserSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseUserSessionProvider(
    private val firebaseAuth: FirebaseAuth,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserSessionProvider {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val state = MutableStateFlow<UserSession?>(null)

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        scope.launch {
            updateFromFirebaseUser(auth.currentUser, forceRefresh = false)
        }
    }

    private val idTokenListener = FirebaseAuth.IdTokenListener { auth ->
        scope.launch {
            updateFromFirebaseUser(auth.currentUser, forceRefresh = false)
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        firebaseAuth.addIdTokenListener(idTokenListener)
        scope.launch { updateFromFirebaseUser(firebaseAuth.currentUser, forceRefresh = false) }
    }

    override val session: Flow<UserSession?>
        get() = state.asStateFlow()

    override suspend fun refreshSession(forceRefresh: Boolean): UserSession? {
        return updateFromFirebaseUser(firebaseAuth.currentUser, forceRefresh)
    }

    override fun currentUserId(): String? = state.value?.userId

    private suspend fun updateFromFirebaseUser(user: FirebaseUser?, forceRefresh: Boolean): UserSession? {
        return withContext(dispatcher) {
            if (user == null) {
                state.emit(null)
                return@withContext null
            }

            val tokenResult = runCatching { fetchIdToken(user, forceRefresh) }.getOrNull()
            val token = tokenResult?.token ?: return@withContext null
            val expiresAt = tokenResult.expirationTimestamp?.times(1000)

            val session = UserSession(
                userId = user.uid,
                idToken = token,
                refreshToken = null,
                expiresAtEpochMillis = expiresAt
            )
            state.emit(session)
            session
        }
    }

    private suspend fun fetchIdToken(user: FirebaseUser, forceRefresh: Boolean): GetTokenResult? {
        return suspendCancellableCoroutine { continuation ->
            val task = user.getIdToken(forceRefresh)
            task.addOnSuccessListener { result ->
                continuation.resume(result)
            }
            task.addOnFailureListener { throwable ->
                if (continuation.isActive) {
                    continuation.resumeWithException(throwable)
                }
            }
            task.addOnCanceledListener {
                if (continuation.isActive) {
                    continuation.cancel()
                }
            }
        }
    }
}
