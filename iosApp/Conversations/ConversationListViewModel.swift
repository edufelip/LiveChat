import Foundation
import Combine
import LiveChatShared

final class ConversationListViewModel: ObservableObject {
    @Published private(set) var conversations: [ConversationListItemViewModel] = []
    @Published var searchQuery: String = ""
    @Published private(set) var isLoading: Bool = false
    @Published var errorMessage: String?

    private let presenter: ConversationListPresenter
    private var stateWatcher: Closeable?

    init(presenter: ConversationListPresenter = IosKoinBridge.shared.conversationListPresenter()) {
        self.presenter = presenter
        bindState()
    }

    deinit {
        stateWatcher?.close()
        presenter.close()
    }

    private func bindState() {
        stateWatcher?.close()
        stateWatcher = presenter.uiState.watch { [weak self] stateAny in
            guard
                let self = self,
                let state = stateAny as? ConversationListUiState
            else { return }
            let summaries: [ConversationSummary] = state.conversations.asArray()
            let mapped = summaries.map { ConversationListItemViewModel(summary: $0) }
            DispatchQueue.main.async {
                self.conversations = mapped
                self.searchQuery = state.searchQuery
                self.isLoading = state.isLoading
                self.errorMessage = state.errorMessage
            }
        }
    }

    func setSearchQuery(_ query: String) {
        presenter.setSearchQuery(query)
    }

    func togglePinned(for conversationId: String, pinned: Bool) {
        presenter.togglePinned(conversationId, pinned: pinned)
    }

    func markAsRead(_ conversationId: String) {
        presenter.markConversationAsRead(conversationId)
    }

    func clearError() {
        presenter.clearError()
    }
}

struct ConversationListItemViewModel: Identifiable, Hashable {
    let id: String
    let title: String
    let subtitle: String
    let timestamp: Double
    let isPinned: Bool
    let unreadCount: Int32

    init(summary: ConversationSummary) {
        id = summary.conversationId
        title = summary.displayName
        subtitle = summary.lastMessage.body
        timestamp = Double(summary.lastMessage.createdAt) / 1000.0
        isPinned = summary.isPinned
        unreadCount = Int32(summary.unreadCount)
    }
}
