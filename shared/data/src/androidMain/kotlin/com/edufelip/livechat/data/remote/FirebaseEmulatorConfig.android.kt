package com.edufelip.livechat.data.remote

data class FirebaseEmulatorConfig(
    val host: String,
    val authPort: Int,
    val firestorePort: Int,
    val storagePort: Int,
    val functionsPort: Int,
)

internal fun loadFirebaseEmulatorConfig(): FirebaseEmulatorConfig? {
    val enabled = readEnvFlag("FIREBASE_EMULATOR_ENABLED")
    if (!enabled) return null
    val host = readEnvValue("FIREBASE_EMULATOR_HOST") ?: "10.0.2.2"
    return FirebaseEmulatorConfig(
        host = host,
        authPort = readEnvInt("FIREBASE_AUTH_EMULATOR_PORT", 9099),
        firestorePort = readEnvInt("FIREBASE_FIRESTORE_EMULATOR_PORT", 8080),
        storagePort = readEnvInt("FIREBASE_STORAGE_EMULATOR_PORT", 9199),
        functionsPort = readEnvInt("FIREBASE_FUNCTIONS_EMULATOR_PORT", 5001),
    )
}

private fun readEnvFlag(key: String): Boolean {
    val value = readEnvValue(key)?.lowercase()
    return value == "1" || value == "true" || value == "yes"
}

private fun readEnvInt(
    key: String,
    defaultValue: Int,
): Int = readEnvValue(key)?.toIntOrNull() ?: defaultValue

private fun readEnvValue(key: String): String? = System.getenv(key) ?: System.getProperty(key)
