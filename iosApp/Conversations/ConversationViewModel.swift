import Foundation
import Combine
import LiveChatShared

final class ConversationViewModel: ObservableObject {
    @Published private(set) var messages: [MessageItemViewModel] = []
    @Published private(set) var isLoading: Bool = false
    @Published private(set) var isSending: Bool = false
    @Published var errorMessage: String?

    private let presenter: ConversationPresenter
    private let sessionProvider: InMemoryUserSessionProvider
    private var stateWatcher: Closeable?
    private var currentConversationId: String = ""

    init(
        conversationId: String,
        presenter: ConversationPresenter = IosKoinBridge.shared.conversationPresenter(),
        sessionProvider: InMemoryUserSessionProvider = IosKoinBridge.shared.sessionProvider()
    ) {
        self.presenter = presenter
        self.sessionProvider = sessionProvider
        start(conversationId: conversationId)
    }

    deinit {
        stateWatcher?.close()
        presenter.close()
    }

    func start(conversationId: String) {
        guard conversationId.isEmpty == false else { return }
        currentConversationId = conversationId
        presenter.start(conversationId: conversationId)
        bindState()
    }

    private func bindState() {
        stateWatcher?.close()
        stateWatcher = presenter.uiState.watch { [weak self] uiState in
            guard let self = self, let state = uiState as? ConversationUiState else { return }
            let currentUserId = self.sessionProvider.currentUserId() ?? ""
            let messages: [Message] = state.messages.asArray()
            let mappedMessages = messages.map { MessageItemViewModel(message: $0, currentUserId: currentUserId) }
            DispatchQueue.main.async {
                self.messages = mappedMessages
                self.isLoading = state.isLoading
                self.isSending = state.isSending
                self.errorMessage = state.errorMessage
            }
        }
    }

    func refresh() {
        presenter.refresh()
    }

    func sendMessage(text: String, completion: (() -> Void)? = nil) {
        presenter.sendMessage(body: text)
        completion?()
    }
}

struct MessageItemViewModel: Identifiable, Hashable {
    let id: String
    let text: String
    let isOwn: Bool
    let timestamp: Date

    init(message: Message, currentUserId: String) {
        id = message.id
        text = message.body
        isOwn = message.senderId == currentUserId
        let seconds = Double(message.createdAt) / 1000.0
        timestamp = Date(timeIntervalSince1970: seconds)
    }
}
