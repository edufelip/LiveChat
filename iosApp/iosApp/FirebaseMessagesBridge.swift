import Foundation
import FirebaseFirestore
import LiveChatCompose
#if canImport(FirebaseAuth)
import FirebaseAuth
#endif

final class FirebaseMessagesBridge: NSObject, MessagesRemoteBridge {
    private let db: Firestore
    private let config: FirebaseRestConfig
    private var listeners: [String: ListenerRegistration] = [:]

    init(config: FirebaseRestConfig) {
        FirebaseConfig.ensureConfiguredForBridge(name: "FirebaseMessagesBridge")
        self.db = Firestore.firestore()
        self.config = config
        super.init()
    }

    func startListening(recipientId: String, listener: MessagesRemoteListener) -> String {
        #if canImport(FirebaseAuth)
        let authUid = Auth.auth().currentUser?.uid ?? "nil"
        NSLog("FirebaseMessagesBridge: startListening recipient=%@ authUid=%@", recipientId, authUid)
        #else
        NSLog("FirebaseMessagesBridge: startListening recipient=%@ authUid=unavailable", recipientId)
        #endif
        let token = UUID().uuidString
        let registration =
            messagesCollection(recipientId: recipientId)
                .addSnapshotListener { snapshot, error in
                    if let error = error {
                        #if canImport(FirebaseAuth)
                        let authUid = Auth.auth().currentUser?.uid ?? "nil"
                        NSLog("FirebaseMessagesBridge: listen error recipient=%@ authUid=%@ error=%@", recipientId, authUid, error.localizedDescription)
                        #else
                        NSLog("FirebaseMessagesBridge: listen error recipient=%@ authUid=unavailable error=%@", recipientId, error.localizedDescription)
                        #endif
                        listener.onError(message: error.localizedDescription)
                        return
                    }
                    let docs = snapshot?.documents ?? []
                    let payloads = docs.map { self.mapDocument($0) }
                    listener.onMessages(messages: payloads)
                }
        listeners[token] = registration
        return token
    }

    func stopListening(token: String) {
        listeners[token]?.remove()
        listeners.removeValue(forKey: token)
    }

    func fetchMessages(recipientId: String, completionHandler: @escaping ([TransportMessagePayload]?, Error?) -> Void) {
        messagesCollection(recipientId: recipientId).getDocuments { snapshot, error in
            if let error = error {
                completionHandler(nil, error)
                return
            }
            let docs = snapshot?.documents ?? []
            completionHandler(docs.map { self.mapDocument($0) }, nil)
        }
    }

    func sendMessage(
        recipientId: String,
        documentId: String,
        payload: TransportMessagePayload,
        completionHandler: @escaping (String?, Error?) -> Void
    ) {
        if let error = validateAuth(for: payload) {
            NSLog("FirebaseMessagesBridge: sendMessage blocked: %@", error.localizedDescription)
            completionHandler(nil, error)
            return
        }
        let document = messagesCollection(recipientId: recipientId).document(documentId)
        document.setData(payload.toFirestorePayload(), merge: false) { error in
            if let error = error {
                let sender = payload.senderId ?? "nil"
                let status = payload.status ?? "nil"
                NSLog("FirebaseMessagesBridge: sendMessage error sender=%@ status=%@ error=%@", sender, status, error.localizedDescription)
                completionHandler(nil, error)
                return
            }
            completionHandler(document.documentID, nil)
        }
    }

    func deleteMessage(
        recipientId: String,
        documentId: String,
        completionHandler: @escaping @Sendable (Error?) -> Void
    ) {
        messagesCollection(recipientId: recipientId)
            .document(documentId)
            .delete { error in
                completionHandler(error)
            }
    }

    func ensureConversation(
        conversationId: String,
        completionHandler: @escaping @Sendable (Error?) -> Void
    ) {
        #if canImport(FirebaseAuth)
        let authUid = Auth.auth().currentUser?.uid
        if authUid == nil {
            let error = NSError(
                domain: "FirebaseMessagesBridge",
                code: -13,
                userInfo: [NSLocalizedDescriptionKey: "FirebaseAuth user is not signed in."]
            )
            NSLog("FirebaseMessagesBridge: ensureConversation blocked conversation=%@ authUid=nil", conversationId)
            completionHandler(error)
            return
        }
        NSLog("FirebaseMessagesBridge: ensureConversation conversation=%@ authUid=%@", conversationId, authUid ?? "nil")
        #endif
        let doc = db.collection(config.conversationsCollection)
            .document(conversationId)
        doc.setData(["created_at": FieldValue.serverTimestamp()], merge: true) { error in
            if let error = error {
                #if canImport(FirebaseAuth)
                let authUid = Auth.auth().currentUser?.uid ?? "nil"
                NSLog("FirebaseMessagesBridge: ensureConversation error conversation=%@ authUid=%@ error=%@", conversationId, authUid, error.localizedDescription)
                #else
                NSLog("FirebaseMessagesBridge: ensureConversation error conversation=%@ authUid=unavailable error=%@", conversationId, error.localizedDescription)
                #endif
            }
            completionHandler(error)
        }
    }


    private func messagesCollection(recipientId: String) -> CollectionReference {
        db.collection(config.conversationsCollection)
            .document(recipientId)
            .collection(config.messagesCollection)
    }

    private func mapDocument(_ doc: QueryDocumentSnapshot) -> TransportMessagePayload {
        let data = doc.data()
        let senderId = data["sender_id"] as? String
        let receiverId = data["receiver_id"] as? String
        let payloadType = data["payload_type"] as? String
        let type = data["type"] as? String
        let content = data["content"] as? String
        let status = data["status"] as? String
        let actionType = data["action_type"] as? String
        let actionMessageId = data["action_message_id"] as? String
        let createdAtMillis =
            (data["created_at_ms"] as? NSNumber)?.int64Value
            ?? (data["created_at"] as? Timestamp).map { Int64($0.dateValue().timeIntervalSince1970 * 1000) }

        let kotlinCreatedAt = createdAtMillis.map { KotlinLong(value: $0) }
        return TransportMessagePayload(
            id: doc.documentID,
            senderId: senderId,
            receiverId: receiverId,
            createdAtMillis: kotlinCreatedAt,
            payloadType: payloadType,
            type: type,
            content: content,
            status: status,
            actionType: actionType,
            actionMessageId: actionMessageId
        )
    }

    private func validateAuth(for payload: TransportMessagePayload) -> NSError? {
        #if canImport(FirebaseAuth)
        guard let senderId = payload.senderId, !senderId.isEmpty else {
            return NSError(
                domain: "FirebaseMessagesBridge",
                code: -10,
                userInfo: [NSLocalizedDescriptionKey: "Missing senderId for message."]
            )
        }
        guard let user = Auth.auth().currentUser else {
            return NSError(
                domain: "FirebaseMessagesBridge",
                code: -11,
                userInfo: [NSLocalizedDescriptionKey: "FirebaseAuth user is not signed in."]
            )
        }
        if user.uid != senderId {
            return NSError(
                domain: "FirebaseMessagesBridge",
                code: -12,
                userInfo: [
                    NSLocalizedDescriptionKey:
                        "SenderId mismatch. payload senderId=\(senderId), auth uid=\(user.uid)"
                ]
            )
        }
        #endif
        return nil
    }
}

private extension TransportMessagePayload {
    func toFirestorePayload() -> [String: Any] {
        var payload: [String: Any] = [
            "sender_id": senderId ?? "",
            "receiver_id": receiverId ?? "",
            "created_at": FieldValue.serverTimestamp(),
            "payload_type": payloadType ?? "message",
            "type": type ?? "text",
            "content": content ?? "",
            "status": status ?? "pending",
        ]
        if let actionType {
            payload["action_type"] = actionType
        }
        if let actionMessageId {
            payload["action_message_id"] = actionMessageId
        }
        if let createdAtMillis = createdAtMillis?.int64Value {
            payload["created_at_ms"] = createdAtMillis
        }
        return payload
    }
}
