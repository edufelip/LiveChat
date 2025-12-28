import Foundation

#if canImport(FirebaseAuth)
import FirebaseAuth
#endif
#if canImport(FirebaseFirestore)
import FirebaseFirestore
#endif
#if canImport(FirebaseFunctions)
import FirebaseFunctions
#endif
#if canImport(FirebaseStorage)
import FirebaseStorage
#endif

enum FirebaseEmulatorSettings {
    static func applyIfNeeded() {
        let environment = ProcessInfo.processInfo.environment
        let enabledFlag = environment["FIREBASE_EMULATOR_ENABLED"]?.lowercased()
        let isEnabled = enabledFlag == "1" || enabledFlag == "true" || enabledFlag == "yes"
        if !isEnabled {
            return
        }

        let host = environment["FIREBASE_EMULATOR_HOST"] ?? "127.0.0.1"
        let authPort = Int(environment["FIREBASE_AUTH_EMULATOR_PORT"] ?? "") ?? 9099
        let firestorePort = Int(environment["FIREBASE_FIRESTORE_EMULATOR_PORT"] ?? "") ?? 8080
        let storagePort = Int(environment["FIREBASE_STORAGE_EMULATOR_PORT"] ?? "") ?? 9199
        let functionsPort = Int(environment["FIREBASE_FUNCTIONS_EMULATOR_PORT"] ?? "") ?? 5001

        #if canImport(FirebaseAuth)
        Auth.auth().useEmulator(withHost: host, port: authPort)
        #endif
        #if canImport(FirebaseFirestore)
        Firestore.firestore().useEmulator(withHost: host, port: firestorePort)
        #endif
        #if canImport(FirebaseStorage)
        Storage.storage().useEmulator(withHost: host, port: storagePort)
        #endif
        #if canImport(FirebaseFunctions)
        Functions.functions().useEmulator(withHost: host, port: functionsPort)
        #endif
    }
}
