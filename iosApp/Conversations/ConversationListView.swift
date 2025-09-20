import SwiftUI
import LiveChatShared

struct ConversationListView: View {
    @ObservedObject var viewModel: ConversationListViewModel
    @State private var selectedConversation: String?

    var body: some View {
        NavigationStack {
            VStack {
                TextField("Search conversations", text: Binding(
                    get: { viewModel.searchQuery },
                    set: { viewModel.setSearchQuery($0) }
                ))
                .textFieldStyle(.roundedBorder)
                .padding()

                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.conversations.isEmpty {
                    Text("No conversations yet")
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        let pinned = viewModel.conversations.filter { $0.isPinned }
                        let others = viewModel.conversations.filter { !$0.isPinned }

                        if !pinned.isEmpty {
                            Section("Pinned") {
                                ForEach(pinned) { item in
                                    ConversationRow(
                                        item: item,
                                        onTogglePin: { pinned in
                                            viewModel.togglePinned(for: item.id, pinned: pinned)
                                        }
                                    )
                                    .contentShape(Rectangle())
                                    .onTapGesture {
                                        viewModel.markAsRead(item.id)
                                        selectedConversation = item.id
                                    }
                                }
                            }
                        }

                        Section(pinned.isEmpty ? "Conversations" : "Others") {
                            ForEach(others) { item in
                                ConversationRow(
                                    item: item,
                                    onTogglePin: { pinned in
                                        viewModel.togglePinned(for: item.id, pinned: pinned)
                                    }
                                )
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    viewModel.markAsRead(item.id)
                                    selectedConversation = item.id
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationDestination(item: $selectedConversation) { conversationId in
                ConversationView(viewModel: ConversationViewModel(conversationId: conversationId))
            }
            .navigationTitle("Chats")
        }
    }
}

private struct ConversationRow: View {
    let item: ConversationListItemViewModel
    let onTogglePin: (Bool) -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(item.title)
                        .font(.headline)
                    if item.isPinned {
                        Image(systemName: "pin.fill")
                            .foregroundColor(.accentColor)
                    }
                }
                Text(item.subtitle)
                    .font(.subheadline)
                    .lineLimit(2)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            if item.unreadCount > 0 {
                Text("\(item.unreadCount)")
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.accentColor.opacity(0.15))
                    .clipShape(Capsule())
            }
            Button {
                onTogglePin(!item.isPinned)
            } label: {
                Image(systemName: item.isPinned ? "pin.slash" : "pin")
            }
            .buttonStyle(.borderless)
        }
        .padding(.vertical, 4)
    }
}

extension String: Identifiable {
    public var id: String { self }
}

// Previews intentionally omitted – Live Kotlin presenters are required.
