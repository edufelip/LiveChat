import Foundation
import LiveChatShared

final class ContactsViewModel: ObservableObject {
    @Published private(set) var localContacts: [ContactUIModel] = []
    @Published private(set) var validatedContacts: [ContactUIModel] = []
    @Published private(set) var isLoading: Bool = false
    @Published private(set) var isSyncing: Bool = false
    @Published var errorMessage: String?

    private let presenter: ContactsPresenter
    private var stateWatcher: Closeable?

    init(presenter: ContactsPresenter = IosKoinBridge.shared.contactsPresenter()) {
        self.presenter = presenter
        bindState()
    }

    deinit {
        stateWatcher?.close()
        presenter.close()
    }

    private func bindState() {
        stateWatcher?.close()
        stateWatcher = presenter.cState.watch { [weak self] stateAny in
            guard
                let self = self,
                let state = stateAny as? ContactsUiState
            else { return }
            let locals: [Contact] = state.localContacts.asArray()
            let validated: [Contact] = state.validatedContacts.asArray()
            DispatchQueue.main.async {
                self.localContacts = locals.map(ContactUIModel.init)
                self.validatedContacts = validated.map(ContactUIModel.init)
                self.isLoading = state.isLoading
                self.isSyncing = state.isSyncing
                self.errorMessage = state.errorMessage
            }
        }
    }

    func syncContacts(_ contacts: [Contact]) {
        presenter.syncContacts(phoneContacts: contacts)
    }

    func invite(contact: ContactUIModel) {
        presenter.inviteContact(contact: contact.toDomain())
    }

    func clearError() {
        presenter.clearError()
    }
}

struct ContactUIModel: Identifiable, Hashable {
    let id: Int32
    let name: String
    let phone: String
    let description: String?
    let photo: String?

    init(contact: Contact) {
        id = contact.id
        name = contact.name
        phone = contact.phoneNo
        description = contact.description_?.value as? String
        photo = contact.photo
    }

    func toDomain() -> Contact {
        Contact(id: id, name: name, phoneNo: phone, description: description, photo: photo)
    }
}
