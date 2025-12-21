import Foundation
import FirebaseFirestore
import LiveChatCompose

final class FirebaseMessagesBridge: NSObject, MessagesRemoteBridge {
    private let db: Firestore
    private let config: FirebaseRestConfig
    private var listeners: [String: ListenerRegistration] = [:]

    init(config: FirebaseRestConfig) {
        self.db = Firestore.firestore()
        self.config = config
        super.init()
    }

    func startListening(recipientId: String, listener: MessagesRemoteListener) -> String {
        let token = UUID().uuidString
        let registration =
            messagesCollection(recipientId: recipientId)
                .addSnapshotListener { snapshot, error in
                    if let error = error {
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
        let document = messagesCollection(recipientId: recipientId).document(documentId)
        document.setData(payload.toFirestorePayload(), merge: false) { error in
            if let error = error {
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
        let doc = db.collection(config.conversationsCollection)
            .document(conversationId)
        doc.setData(["created_at": FieldValue.serverTimestamp()], merge: true) { error in
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
        let type = data["type"] as? String
        let content = data["content"] as? String
        let status = data["status"] as? String
        let createdAtMillis =
            (data["created_at_ms"] as? NSNumber)?.int64Value
            ?? (data["created_at"] as? Timestamp).map { Int64($0.dateValue().timeIntervalSince1970 * 1000) }

        let kotlinCreatedAt = createdAtMillis.map { KotlinLong(value: $0) }
        return TransportMessagePayload(
            id: doc.documentID,
            senderId: senderId,
            receiverId: receiverId,
            createdAtMillis: kotlinCreatedAt,
            type: type,
            content: content,
            status: status
        )
    }
}

private extension TransportMessagePayload {
    func toFirestorePayload() -> [String: Any] {
        var payload: [String: Any] = [
            "sender_id": senderId ?? "",
            "receiver_id": receiverId ?? "",
            "created_at": FieldValue.serverTimestamp(),
            "type": type ?? "text",
            "content": content ?? "",
            "status": status ?? "pending",
        ]
        if let createdAtMillis = createdAtMillis?.int64Value {
            payload["created_at_ms"] = createdAtMillis
        }
        return payload
    }
}
