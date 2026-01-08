import Foundation
import FirebaseFirestore
import FirebaseFunctions
import LiveChatCompose

final class FirebaseContactsBridge: NSObject, ContactsRemoteBridge {
    private lazy var db: Firestore = Firestore.firestore()
    private lazy var functions: Functions = Functions.functions()
    private let config: FirebaseRestConfig

    init(config: FirebaseRestConfig) {
        FirebaseConfig.ensureConfiguredForBridge(name: "FirebaseContactsBridge")
        self.config = config
        super.init()
    }

    func phoneExists(phoneE164: String, completionHandler: @escaping (PhoneExistsSingleResult?, Error?) -> Void) {
        if let error = ensureFirebaseConfigured() {
            completionHandler(nil, error)
            return
        }
        functions
            .httpsCallable("phoneExists")
            .call(["phone": phoneE164]) { result, error in
                if let error = error {
                    completionHandler(nil, error)
                    return
                }
                let data = result?.data as? [String: Any] ?? [:]
                let exists = data["exists"] as? Bool ?? false
                let uid = data["uid"] as? String
                let response = PhoneExistsSingleResult(exists: exists, uid: uid)
                completionHandler(response, nil)
            }
    }

    func phoneExistsMany(phones: [String], completionHandler: @escaping (PhoneExistsBatchResult?, Error?) -> Void) {
        if let error = ensureFirebaseConfigured() {
            completionHandler(nil, error)
            return
        }
        if phones.isEmpty {
            completionHandler(PhoneExistsBatchResult(registeredPhones: [], matches: []), nil)
            return
        }
        functions
            .httpsCallable("phoneExistsMany")
            .call(["phones": phones]) { result, error in
                if let error = error {
                    completionHandler(nil, error)
                    return
                }
                let data = result?.data as? [String: Any] ?? [:]
                let registered = data["registered"] as? [String] ?? []
                let matchesPayload = data["matches"] as? [[String: Any]] ?? []
                let matches: [PhoneExistsMatch] = matchesPayload.compactMap { payload in
                    guard let phone = payload["phone"] as? String,
                          let uid = payload["uid"] as? String else {
                        return nil
                    }
                    return PhoneExistsMatch(phone: phone, uid: uid)
                }
                completionHandler(PhoneExistsBatchResult(registeredPhones: registered, matches: matches), nil)
            }
    }

    func isUserRegistered(phoneE164: String, completionHandler: @escaping (KotlinBoolean?, Error?) -> Void) {
        if let error = ensureFirebaseConfigured() {
            completionHandler(nil, error)
            return
        }
        db.collection(config.usersCollection)
            .whereField("phone_num", isEqualTo: phoneE164)
            .limit(to: 1)
            .getDocuments { snapshot, error in
                if let error = error {
                    completionHandler(nil, error)
                    return
                }
                let exists = (snapshot?.documents.isEmpty == false)
                completionHandler(KotlinBoolean(value: exists), nil)
            }
    }

    private func ensureFirebaseConfigured() -> NSError? {
        if FirebaseConfig.configureIfNeeded() == false {
            return NSError(
                domain: "FirebaseContactsBridge",
                code: -1,
                userInfo: [
                    NSLocalizedDescriptionKey:
                        "Firebase is not configured. Ensure GoogleService-Info.plist is in the app bundle and the bundle ID matches.",
                ]
            )
        }
        if let missing = FirebaseConfig.missingRequiredOptions() {
            return NSError(
                domain: "FirebaseContactsBridge",
                code: -2,
                userInfo: [NSLocalizedDescriptionKey: missing]
            )
        }
        return nil
    }
}
