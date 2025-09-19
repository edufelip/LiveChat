package com.project.livechat.data.di

import com.project.livechat.data.remote.FirebaseRestConfig
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

private const val DEFAULT_COLLECTION = "users"
private const val PROJECT_ID_KEY = "PROJECT_ID"
private const val API_KEY_KEY = "API_KEY"
private const val COLLECTION_KEY = "FIRESTORE_COLLECTION"

@Throws(IllegalStateException::class)
fun loadFirebaseRestConfigFromPlist(
    bundle: NSBundle = NSBundle.mainBundle,
    resourceName: String = "GoogleService-Info",
    usersCollectionOverride: String? = null
): FirebaseRestConfig {
    val path = bundle.pathForResource(resourceName, ofType = "plist")
        ?: error("$resourceName.plist not found in bundle. Ensure Firebase configuration is copied to the iOS target.")

    val dictionary = NSDictionary.dictionaryWithContentsOfFile(path)
        ?: error("Unable to read $resourceName.plist. Check that it has a valid plist structure.")

    val projectId = dictionary.objectForKey(PROJECT_ID_KEY) as? String
        ?: error("PROJECT_ID is missing from $resourceName.plist.")

    val apiKey = dictionary.objectForKey(API_KEY_KEY) as? String
        ?: error("API_KEY is missing from $resourceName.plist.")

    val usersCollection = usersCollectionOverride
        ?: (dictionary.objectForKey(COLLECTION_KEY) as? String)
        ?: DEFAULT_COLLECTION

    return FirebaseRestConfig(
        projectId = projectId,
        apiKey = apiKey,
        usersCollection = usersCollection
    )
}
