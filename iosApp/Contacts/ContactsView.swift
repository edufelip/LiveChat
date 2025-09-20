import SwiftUI
import LiveChatShared

struct ContactsView: View {
    @ObservedObject var viewModel: ContactsViewModel
    let phoneContactsProvider: () -> [Contact]

    var body: some View {
        NavigationStack {
            VStack {
                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        Section("Contacts") {
                            ForEach(displayContacts, id: \.id) { contact in
                                HStack {
                                    VStack(alignment: .leading) {
                                        Text(contact.name)
                                        Text(contact.phone)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                    Spacer()
                                    if viewModel.validatedContacts.contains(contact) {
                                        Text("On LiveChat")
                                            .font(.caption)
                                            .foregroundStyle(.green)
                                    } else {
                                        Button("Invite") {
                                            viewModel.invite(contact: contact)
                                        }
                                        .buttonStyle(.bordered)
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Contacts")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Sync") {
                        let contacts = phoneContactsProvider()
                        viewModel.syncContacts(contacts)
                    }
                    .disabled(viewModel.isSyncing)
                }
            }
            .alert("Error", isPresented: Binding<Bool>(
                get: { viewModel.errorMessage != nil },
                set: { _ in viewModel.clearError() }
            ), actions: {
                Button("OK", role: .cancel) {
                    viewModel.clearError()
                }
            }, message: {
                Text(viewModel.errorMessage ?? "")
            })
        }
    }

    private var displayContacts: [ContactUIModel] {
        let local = viewModel.localContacts
        return Array(Set(local + viewModel.validatedContacts)).sorted { $0.name < $1.name }
    }
}
