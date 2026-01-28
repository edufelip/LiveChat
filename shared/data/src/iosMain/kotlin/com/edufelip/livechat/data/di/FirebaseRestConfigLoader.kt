package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.remote.FirebaseRestConfig
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSProcessInfo
import platform.Foundation.dictionaryWithContentsOfFile

private const val DEFAULT_USERS_COLLECTION = "users"
private const val DEFAULT_MESSAGES_COLLECTION = "items"
private const val DEFAULT_CONVERSATIONS_COLLECTION = "inboxes"
private const val DEFAULT_INVITES_COLLECTION = "invites"
private const val DEFAULT_WEBSOCKET_ENDPOINT = ""
private const val DEFAULT_POLLING_INTERVAL_MS = 5_000L

private const val PROJECT_ID_KEY = "PROJECT_ID"
private const val API_KEY_KEY = "API_KEY"
private const val USERS_COLLECTION_KEY = "FIRESTORE_COLLECTION"
private const val MESSAGES_COLLECTION_KEY = "FIRESTORE_MESSAGES_COLLECTION"
private const val CONVERSATIONS_COLLECTION_KEY = "FIRESTORE_CONVERSATIONS_COLLECTION"
private const val WEBSOCKET_ENDPOINT_KEY = "FIRESTORE_WEBSOCKET_ENDPOINT"
private const val POLLING_INTERVAL_KEY = "FIRESTORE_POLLING_INTERVAL_MS"
private const val INVITES_COLLECTION_KEY = "FIRESTORE_INVITES_COLLECTION"
private const val DEFAULT_REGION_ISO_KEY = "DEFAULT_REGION_ISO"

@Throws(IllegalStateException::class)
fun loadFirebaseRestConfigFromPlist(
    bundle: NSBundle = NSBundle.mainBundle,
    resourceName: String = "GoogleService-Info",
    usersCollectionOverride: String? = null,
): FirebaseRestConfig {
    val path =
        bundle.pathForResource(resourceName, ofType = "plist")
            ?: error("$resourceName.plist not found in bundle. Ensure Firebase configuration is copied to the iOS target.")

    val dictionary =
        NSDictionary.dictionaryWithContentsOfFile(path)
            ?: error("Unable to read $resourceName.plist. Check that it has a valid plist structure.")

    val projectId =
        dictionary[PROJECT_ID_KEY] as? String
            ?: error("PROJECT_ID is missing from $resourceName.plist.")

    val apiKey =
        dictionary[API_KEY_KEY] as? String
            ?: error("API_KEY is missing from $resourceName.plist.")

    val usersCollection =
        usersCollectionOverride
            ?: (dictionary[USERS_COLLECTION_KEY] as? String)
            ?: DEFAULT_USERS_COLLECTION

    val messagesCollection =
        (dictionary[MESSAGES_COLLECTION_KEY] as? String)
            ?: DEFAULT_MESSAGES_COLLECTION

    val conversationsCollection =
        (dictionary[CONVERSATIONS_COLLECTION_KEY] as? String)
            ?: DEFAULT_CONVERSATIONS_COLLECTION

    val websocketEndpoint =
        (dictionary[WEBSOCKET_ENDPOINT_KEY] as? String)
            ?: DEFAULT_WEBSOCKET_ENDPOINT

    val pollingIntervalMs =
        ((dictionary[POLLING_INTERVAL_KEY] as? NSNumber)?.longLongValue)?.toLong()
            ?: DEFAULT_POLLING_INTERVAL_MS

    val invitesCollection =
        (dictionary[INVITES_COLLECTION_KEY] as? String)
            ?: DEFAULT_INVITES_COLLECTION
    val defaultRegionIso = dictionary[DEFAULT_REGION_ISO_KEY] as? String
    val emulator = iosEmulatorConfig()

    return FirebaseRestConfig(
        projectId = projectId,
        apiKey = apiKey,
        emulatorHost = emulator?.host,
        emulatorPort = emulator?.firestorePort,
        usersCollection = usersCollection,
        messagesCollection = messagesCollection,
        conversationsCollection = conversationsCollection,
        invitesCollection = invitesCollection,
        websocketEndpoint = websocketEndpoint,
        pollingIntervalMs = pollingIntervalMs,
        defaultRegionIso = defaultRegionIso,
    )
}

private data class IosEmulatorConfig(
    val host: String,
    val firestorePort: Int,
)

private fun iosEmulatorConfig(): IosEmulatorConfig? {
    val environment = NSProcessInfo.processInfo.environment
    val enabled =
        environment["FIREBASE_EMULATOR_ENABLED"]?.toString()?.lowercase() in listOf("1", "true", "yes")
    if (!enabled) return null
    val host = environment["FIREBASE_EMULATOR_HOST"]?.toString()?.ifBlank { null } ?: "127.0.0.1"
    val port =
        environment["FIREBASE_FIRESTORE_EMULATOR_PORT"]?.toString()?.toIntOrNull()
            ?: 8080
    return IosEmulatorConfig(host = host, firestorePort = port)
}
