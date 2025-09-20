import SwiftUI
import LiveChatShared

@main
struct LiveChatIOSApp: App {
    @StateObject private var conversationViewModel: ConversationViewModel

    init() {
        let config = FirebaseRestConfig(
            projectId: "YOUR_FIREBASE_PROJECT",
            apiKey: "YOUR_FIREBASE_API_KEY",
            usersCollection: "users",
            messagesCollection: "messages",
            conversationsCollection: "conversations",
            websocketEndpoint: "",
            pollingIntervalMs: 5_000
        )
        KoinHolder.shared.startIfNeeded(config: config)
        KoinHolder.shared.updateSession(userId: "demo-user")
        _conversationViewModel = StateObject(wrappedValue: ConversationViewModel(conversationId: "demo-conversation"))
    }

    var body: some Scene {
        WindowGroup {
            NavigationView {
                ConversationView(viewModel: conversationViewModel)
            }
        }
    }
}
