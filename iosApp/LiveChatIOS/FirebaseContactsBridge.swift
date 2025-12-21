import Foundation
import FirebaseFirestore
import FirebaseFunctions
import LiveChatCompose

final class FirebaseContactsBridge: NSObject, ContactsRemoteBridge {
    private let db: Firestore
    private let functions: Functions
    private let config: FirebaseRestConfig

    init(config: FirebaseRestConfig) {
        self.db = Firestore.firestore()
        self.functions = Functions.functions()
        self.config = config
        super.init()
    }

    func phoneExists(phoneE164: String, completionHandler: @escaping (PhoneExistsSingleResult?, Error?) -> Void) {
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
}
