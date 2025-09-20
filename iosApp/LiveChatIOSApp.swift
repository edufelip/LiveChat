import SwiftUI
import LiveChatShared

@main
struct LiveChatIOSApp: App {
    @StateObject private var conversationListViewModel: ConversationListViewModel
    @StateObject private var contactsViewModel: ContactsViewModel

    init() {
        let config = FirebaseRestConfig(
            projectId: "YOUR_FIREBASE_PROJECT",
            apiKey: "YOUR_FIREBASE_API_KEY",
            usersCollection: "users",
            messagesCollection: "messages",
            conversationsCollection: "conversations",
            invitesCollection: "invites",
            websocketEndpoint: "",
            pollingIntervalMs: 5_000
        )
        KoinHolder.shared.startIfNeeded(config: config)
        KoinHolder.shared.updateSession(userId: "demo-user")
        _conversationListViewModel = StateObject(wrappedValue: ConversationListViewModel())
        _contactsViewModel = StateObject(wrappedValue: ContactsViewModel())
    }

    var body: some Scene {
        WindowGroup {
            TabView {
                ConversationListView(viewModel: conversationListViewModel)
                    .tabItem {
                        Label("Chats", systemImage: "message.fill")
                    }
                ContactsView(viewModel: contactsViewModel, phoneContactsProvider: { [] })
                    .tabItem {
                        Label("Contacts", systemImage: "person.2.fill")
                    }
            }
        }
    }
}
